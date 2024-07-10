package com.car.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.car.validation.Find_idFormValidation;
import com.car.validation.Find_pwFormValidation;
import com.car.validation.InquiryWriteValidation;
import com.car.validation.LoginFormValidation;
import com.car.validation.SignupFormValidation;
import com.car.validation.Update_pwFormValidation;
import com.car.dto.Car;
import com.car.dto.Inquiry;
import com.car.dto.Member;
import com.car.exception.CodeNumberException;
import com.car.exception.ValidationException;
import com.car.exception.errorcode.ErrorCode;
import com.car.service.BookMarkService;
import com.car.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.internal.build.AllowSysOut;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

	@Value("${pw-role.password-rejex}")
	private String passwordRegex;

	@Value("${coolsms.api.key}")
	private String APIKEY;

	@Value("${coolsms.api.secret}")
	private String SECRETKEY;

	@Value("${coolsms.api.form_number}")
	private String FROM_NUMBER;

	private final BookMarkService bookmarkService;

	private final MemberService memberService;

	private final PasswordEncoder passwordEncoder;

	@ModelAttribute("member")
	public Member setMember() {
		return new Member(); // 기본 Member 객체를 세션에 저장
	}

	@RequestMapping("/")
	public String first_home() {
		return "redirect:/home";
	}
	@RequestMapping("/test")
	public String first_hometest() {
		return "/test.html";
	}
	/* 세션 초기화 */
	@RequestMapping("/user_logout")
	public String backhome(HttpServletRequest request, HttpServletResponse response) {
		// 세션을 삭제
		HttpSession session = request.getSession(false);
		// session이 null이 아니라는건 기존에 세션이 존재했었다는 뜻이므로
		// 세션이 null이 아니라면 session.invalidate()로 세션 삭제해주기.
		if (session != null) {
			session.invalidate();
			System.out.println("세션이 무효화되었습니다.");
		} else {
			System.out.println("세션이 존재하지 않습니다.");
		}
		// 캐시 제어 헤더 추가
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);

		return "redirect:/home";
	}

	@GetMapping("/home")
	public String goHome(HttpSession session, Model model,@ModelAttribute("InquiryWriteValidation") InquiryWriteValidation InquiryValidation) {
		
		// 즐겨찾기 Top10 캐로셀
		List<Car> top10Cars = bookmarkService.getBookmarkTop10Cars();
		model.addAttribute("top10Cars", top10Cars);
		model.addAttribute("inquiry", new Inquiry());
				return "homepage/home";
	}
	
	/*
	 * 회원가입 view
	 */
	@GetMapping("/signup")
	public String joinView(@ModelAttribute("SignupFormValidation") SignupFormValidation member) {
		return "member/signup.html";
	}

	/*
	 * 회원가입
	 */
	@PostMapping("/signup")
	public String join_result(@Validated @ModelAttribute("SignupFormValidation") SignupFormValidation member,
			BindingResult bindingResult, Model model) {

		if (bindingResult.hasErrors()) {

			Map<String, String> errors = new HashMap<>();
			// 필드별로 발생한 모든 오류 메시지를 맵에 담음
			bindingResult.getFieldErrors().forEach(error -> {
				String fieldName = error.getField();
				String errorMessage = error.getDefaultMessage();
				
				errors.put(fieldName, errorMessage);

			});
			// 오류 메시지를 모델에 추가
			errors.forEach((fieldName, errorMessage) -> model.addAttribute(fieldName + "Error", errorMessage));
			return "member/signup";
		}

		List<Member> findmemberEmail = memberService.findByMemberEmail(member.getMemberEmail());
		List<Member> findmemberNickname = memberService.findByMemberNickname(member.getMemberNickname());
		Member findmemberId = memberService.findByMemberId(member.getMemberId());
		List<Member> findmemberPhone = memberService.findByMemberPhoneNum(member.getMemberPhoneNum());

		
		
		/*
		 * 아이디중복검사
		 */
		if (findmemberId != null && findmemberId.getMemberId().equals(member.getMemberId())) {
			model.addAttribute("memberIdError", "존재하는 아이디입니다.");
		}
		/*
		 * 닉네임 중복검사
		 */
		if (!findmemberNickname.isEmpty()) {
			model.addAttribute("memberNicknameError", "존재하는 닉네임입니다.");
		}
		/*
		 * 이메일중복검사
		 */
		if (!findmemberEmail.isEmpty()) {
			model.addAttribute("memberEmailError", "존재하는 이메일입니다.");
		}
		/*
		 * 전화번호 중복검사
		 */
		if (!findmemberPhone.isEmpty()) {
			model.addAttribute("memberPhoneNumError", "존재하는 전화번호입니다");
		}
		
		if(model.getAttribute("memberIdError") !=null || model.getAttribute("memberNicknameError") !=null || model.getAttribute("memberEmailError") !=null || model.getAttribute("memberPhoneNumError") !=null) {
			return "member/signup";
		}
		String EncoderPw = passwordEncoder.encode(member.getMemberPw());

		Member Member = new Member();
		Member.setMemberEmail(member.getMemberEmail());
		Member.setMemberId(member.getMemberId());
		Member.setMemberPw(EncoderPw);
		Member.setMemberName(member.getMemberName());
		Member.setMemberNickname(member.getMemberNickname());
		Member.setMemberPhoneNum(member.getMemberPhoneNum());
		Member.setMemberSocialNickname("");
		Member.setMemberSocial("user");
		Member.setMemberRole("ROLE_USER");

		Member save_member = memberService.insertMember(Member);

		model.addAttribute("msg", "성공적으로 회원가입이 되었습니다.");
		model.addAttribute("url", "/member_login");
		return "alert";

	}

	/*
	 * 로그인 view
	 */
	@GetMapping("/member_login")
	public String loginView(@ModelAttribute("LoginFormValidation") LoginFormValidation memberm,
			@RequestParam(value = "redirectURL", defaultValue = "/home") String redirectURL, Model model) {
		model.addAttribute("redirectURL", redirectURL);

		return "member/login.html";
	}


	/*
	 * 로그인
	 */
	@PostMapping("/member_login_check")
	public String login_result(@Validated @ModelAttribute("LoginFormValidation") LoginFormValidation membercheck,
			BindingResult bindingResult,
			@RequestParam(value = "redirectURL", defaultValue = "/home") String redirectURL, Model model,
			HttpServletRequest request, HttpSession session) {

		String memberId = membercheck.getMemberId();
		String memberPw = membercheck.getMemberPw();

		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			// 필드별로 발생한 모든 오류 메시지를 맵에 담음
			bindingResult.getFieldErrors().forEach(error -> {
				String fieldName = error.getField();
				String errorMessage = error.getDefaultMessage();
				// 비밀번호 오류 처리
				if (membercheck.getMemberPw() != "" && "memberPw".equals(fieldName)) {
					if (!errors.containsKey(fieldName)) {
						errors.put(fieldName, errorMessage);
					}
				} else {
					// 다른 필드 오류 처리
					errors.put(fieldName, errorMessage);
				}

			});
			// 오류 메시지를 모델에 추가
			errors.forEach((fieldName, errorMessage) -> model.addAttribute(fieldName + "Error", errorMessage));

			return "member/login";
		}

		Member findmember = memberService.findMember(memberId);

		if (findmember == null) {
			model.addAttribute("memberIdError", "존재하지 않는 아이디입니다.");
			return "member/login";
		}

		boolean passwordCheck = memberService.passwordCheck(findmember, memberPw);

		// 사용자가 존재하고 비밀번호가 일치하는지 확인
		if (findmember != null && passwordCheck) {

			// 로그인 성공 시 세션에 멤버정보 저장하고 홈페이지로 이동
			session.setAttribute("user", findmember);

			if (redirectURL.contains("/mypage/bookmark/")) {
				return "redirect:/model/getModelList";
			}

			if (redirectURL.contains("/board/getBoard")) {
				return "redirect:/board/getBoardList";
			}

			if (redirectURL.contains("/model/bookmark/")) {
				int carId = Integer.parseInt(redirectURL.substring(redirectURL.lastIndexOf("/") + 1));
				System.out.println(carId);
				return "redirect:/model/getModel?carId=" + carId;
			}

			return "redirect:" + redirectURL;
		} else {
			model.addAttribute("memberPwError", "비밀번호가 일치하지 않습니다.");
			return "member/login";
		}
	}

	/*
	 * 로그아웃
	 * 
	 * @PostMapping("/logout") public String logout(HttpServletRequest request) { //
	 * 세션을 삭제 HttpSession session = request.getSession(false); // session이 null이
	 * 아니라는건 기존에 세션이 존재했었다는 뜻이므로 // 세션이 null이 아니라면 session.invalidate()로 세션 삭제해주기.
	 * if (session != null) { session.invalidate(); } return "redirect:/"; }
	 */
	/*
	 * 아이디 찾기 폼
	 */
	@GetMapping("/find_idForm")
	public String find_idForm(@ModelAttribute("Find_idFormValidation") Find_idFormValidation find_idFormValidation) {
		return "member/find_id";
	}

	/*
	 * 비밀번호 찾기 폼
	 */
	@GetMapping("/find_pwForm")
	public String find_pwForm(@ModelAttribute("Find_pwFormValidation") Find_pwFormValidation find_pwFormValidation,
			@ModelAttribute("Update_pwFormValidation") Update_pwFormValidation update_pwFormValidation) {

		return "member/find_pw";
	}

	/*
	 * 아이디찾기(인증번호 전송하기)
	 */
	@GetMapping("/find_id")
	public ResponseEntity<Map<String, Object>> find_id(
			@Validated @ModelAttribute("Find_idFormValidation") Find_idFormValidation find_idFormValidation,
			BindingResult bindingResult, HttpSession session, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			// 필드별로 발생한 모든 오류 메시지를 맵에 담음
			bindingResult.getFieldErrors().forEach(error -> {
				String fieldName = error.getField();
				String errorMessage = error.getDefaultMessage();
				errors.put(fieldName, errorMessage);
			});

			throw new ValidationException(errors);
		}

		Member findmember = memberService.SMSfindMember(find_idFormValidation.getMemberName(),
				find_idFormValidation.getMemberPhoneNumber(), session);
		if (findmember == null) {
			Map<String, String> errors = new HashMap<>();
			bindingResult.getFieldErrors().forEach(error -> {
				String fieldName = error.getField();
				String errorMessage = error.getDefaultMessage();
				errors.put(fieldName, errorMessage);
			});
			throw new ValidationException(errors);

		} else if (find_idFormValidation.getMemberName() != null
				&& findmember.getMemberName().equals(find_idFormValidation.getMemberName())) {
			if (findmember.getMemberPhoneNum().equals(find_idFormValidation.getMemberPhoneNumber())) {
				sendmessage(find_idFormValidation.getMemberPhoneNumber(), request);
				response.put("success", true);
				response.put("message", "인증번호가 요청되었습니다.");
				return ResponseEntity.ok(response);
			}
		}
		response.put("message", "회원정보가 일치하지 않습니다.");
		response.put("success", false);
		return ResponseEntity.ok(response);
	}

	/*
	 * 비밀번호찾기(인증번호 전송하기)
	 */
	@GetMapping("/find_pw")
	public ResponseEntity<Map<String, Object>> find_pw(
			@Validated @ModelAttribute("find_pwFormValidation") Find_pwFormValidation find_pwFormValidation,
			BindingResult bindingResult,

			HttpSession session, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			// 필드별로 발생한 모든 오류 메시지를 맵에 담음
			bindingResult.getFieldErrors().forEach(error -> {
				String fieldName = error.getField();
				String errorMessage = error.getDefaultMessage();
				errors.put(fieldName, errorMessage);
			});
			throw new ValidationException(errors);

		}
		Member findmember = memberService.SMSfindMemberPw(find_pwFormValidation.getMemberId(),
				find_pwFormValidation.getMemberPhoneNumber(), session);

		if (findmember == null) {
			Map<String, String> errors = new HashMap<>();
			bindingResult.getFieldErrors().forEach(error -> {
				String fieldName = error.getField();
				String errorMessage = error.getDefaultMessage();
				errors.put(fieldName, errorMessage);
			});

			throw new ValidationException(errors);

		} else if (find_pwFormValidation.getMemberId() != null
				&& findmember.getMemberId().equals(find_pwFormValidation.getMemberId())) {
			if (findmember.getMemberPhoneNum().equals(find_pwFormValidation.getMemberPhoneNumber())) {

				sendmessage(find_pwFormValidation.getMemberPhoneNumber(), request);
				response.put("success", true);
				response.put("message", "인증번호가 요청되었습니다.");
				return ResponseEntity.ok(response);
			}
		}

		response.put("message", "회원정보가 일치하지 않습니다.");
		response.put("success", false);
		return ResponseEntity.ok(response);
	}

	/*
	 * 아이디 찾기(인증번호 확인)
	 */
	@GetMapping("/find_id_code_check")
	public ResponseEntity<Map<String, Object>> find_id_code_check(@RequestParam("code") String code,
			HttpServletRequest request, Model model, HttpSession session) {

		Map<String, Object> response = new HashMap<>();

		if (code.equals(session.getAttribute("codeNumber")) && session.getAttribute("find_idMember") != null) {

			session.removeAttribute("codeNumber");

			response.put("success", true);
			response.put("memberId", ((Member) session.getAttribute("find_idMember")).getMemberId());
			session.removeAttribute("find_idMember");
			return ResponseEntity.ok(response);
		} else {
			throw new CodeNumberException(ErrorCode.CODE_NUMBER_MISMATCH, null);

		}
	}

	/*
	 * 비밀번호 찾기(인증번호 확인)
	 */
	@GetMapping("/find_pw_code_check")
	public ResponseEntity<Map<String, Object>> find_pw_code_check(@RequestParam("code") String code,
			HttpServletRequest request, Model model, HttpSession session) {

		Map<String, Object> response = new HashMap<>();
		if (code.equals(session.getAttribute("codeNumber")) && session.getAttribute("find_pwMember") != null) {
			session.removeAttribute("codeNumber");

			response.put("message", "인증번호가 일치합니다.");
			response.put("success", true);
			return ResponseEntity.ok(response);
		} else {
			throw new CodeNumberException(ErrorCode.CODE_NUMBER_MISMATCH, null);
		}
	}

	@PostMapping("/update_pw")
	public ResponseEntity<Map<String, Object>> update_pw(
			@RequestParam(value = "memberPw", required = false) String memberPw,
			@Validated @ModelAttribute("Update_pwFormValidation") Update_pwFormValidation update_pwFormValidation,
			BindingResult bindingResult, HttpServletRequest request, Model model, HttpSession session) {

		Map<String, Object> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			// 필드별로 발생한 모든 오류 메시지를 맵에 담음
			bindingResult.getFieldErrors().forEach(error -> {
				String fieldName = error.getField();
				String errorMessage = error.getDefaultMessage();

				errors.put(fieldName, errorMessage);
			});
			throw new ValidationException(errors);
		} else {

			memberService.updatepw(((Member) session.getAttribute("find_pwMember")).getMemberId(), memberPw);

			response.put("message", "비밀번호가 수정되었습니다.");
			response.put("success", true);
			response.put("redirectUrl", "/find_pwForm");
			session.removeAttribute("find_pwMember");
			return ResponseEntity.ok(response);
		}
	}

	/*
	 * =============================================================================
	 * =======
	 */

	/*
	 * 인증번호 메세지 뿌리기
	 */
	public SingleMessageSentResponse sendmessage(String phone, HttpServletRequest request) {
		String codeNumber = RandomStringUtils.randomNumeric(6);
		HttpSession session = request.getSession(false);
		session.setAttribute("codeNumber", codeNumber);
		System.out.println(session.getAttribute("codeNumber"));
		SingleMessageSentResponse response = memberService.sendmessage(phone, codeNumber, APIKEY, SECRETKEY,
				FROM_NUMBER);

		return response;
	}
}
