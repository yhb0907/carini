package com.car.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Optional;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import com.car.dto.Member;
import com.car.service.BookMarkService;
import com.car.service.MemberService;
import com.car.service.SocialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SocialController {

	private final SocialService socialService;

	@Value("${kakao.client-id}")
	private String clientId;

	@Value("${kakao.redirect-uri}")
	private String redirectUri;

	@ModelAttribute("member")
	public Member setMember() {
		return new Member(); // 기본 Member 객체를 세션에 저장
	}

	/*
	 * 클라이언트id와redirectURi를 넣어서 리다이렉트설정
	 */
	@GetMapping("/oauth/kakao")
	public RedirectView redirectToKakao() {
		String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId + "&redirect_uri="
				+ redirectUri + "&response_type=code";
		return new RedirectView(kakaoAuthUrl);
	}

	/*
	 * 카카오 - 인카코드로 토큰을 받고 토근으로 정보 추출
	 */

	@RequestMapping(value = "/login")
	public String login(@RequestParam("code") String code, Model model, Member member, HttpServletRequest request,
			HttpSession session) {

		// 1번 인증코드로 요청 전달
		String accessToken = socialService.getKakaoAccessToken(code, clientId, redirectUri);

		// 2번 인증코드로 토큰 전달
		HashMap<String, Object> userInfo = socialService.getKakaoUserInfo(accessToken);

		/*
		 * 카카오 - 받은 정보로 비회원이라면 회원가입
		 */
		if (userInfo == null) {
			return "/login";
		} else {
			System.out.println("login info :" + userInfo.toString());

			HashMap<String, Object> token = new HashMap<>();
			token.put("url", "/kakao/signUp");
			token.put("userInfo", userInfo);

			System.out.println("name = {}" + userInfo.get("nickname"));
			System.out.println("email = {}" + userInfo.get("email"));

			member.setMemberEmail((String) userInfo.get("email"));
			member.setMemberNickname((String) userInfo.get("nickname"));
			member.setMemberSocialNickname("");
			// 카카오 회원정보 데이터베이스 넣기!
			Member save_member = socialService.kakaoSignUp(member);
			System.out.println(save_member.getMemberEmail());

			// member데이터를 세션에 저장
			session.setAttribute("user", save_member);

			return "redirect:/home";
		}
	}

	@RequestMapping(value = "/logout")
	public ModelAndView logout(@RequestBody Member member, HttpSession session) {

		ModelAndView mav = new ModelAndView();

		socialService.kakaoLogout((String) session.getAttribute("access_token"));
		session.removeAttribute("accessToken");
		session.removeAttribute("userId");
		mav.setViewName("index");

		return mav;

	}

	// 네이버=====================================================================

	// application.yml에서 필요한 정보 가져오기
	@Value("${naver.client-id}")
	private String client_id;
	@Value("${naver.client-secret}")
	private String client_secret;
	@Value("${naver.redirect-uri}")
	private String redirect_uri;
	@Value("${naver.request-token-uri}")
	private String request_token_uri;

	// 인가 코드 받기
	@GetMapping("/api/naver/oauth")
	public String naverConnect() {

		// state용 난수 생성
		SecureRandom random = new SecureRandom();
		String state = new BigInteger(130, random).toString(32);

		// redirect
		StringBuffer url = new StringBuffer();

		url.append("http://nid.naver.com/oauth2.0/authorize?");
		url.append("&response_type=code");
		url.append("&client_id=" + client_id);
		url.append("&redirect_uri=" + redirect_uri);
		url.append("&state=" + state);

		return "redirect:" + url;
	}

	@RequestMapping(value = "/api/naver/callback", method = { RequestMethod.GET, RequestMethod.POST })
	public String naverLogin(@RequestParam(value = "code") String code, @RequestParam(value = "state") String state,
			Model model, Member member, HttpServletRequest request, HttpSession session) {
		String token = socialService.getNaverAccessToken(code, state, client_id, client_secret);
		HashMap<String, Object> userInfo = socialService.getNaverUserInfo(token);

		// 네이버 - 받은 정보로 비회원이라면 회원가입

		if (userInfo == null) {
			return "/api/naver/callback";
		} else {

			member.setMemberId((String) userInfo.get("id"));
			member.setMemberName((String) userInfo.get("name"));
			member.setMemberNickname((String) userInfo.get("nickname"));
			member.setMemberEmail((String) userInfo.get("email"));
			member.setMemberPhoneNum((String) userInfo.get("mobile"));
			member.setMemberSocialNickname("");
			// 네이버 회원정보 데이터베이스 넣기!
			Member save_member = socialService.naverSignUp(member);
			Member memberOne = socialService.findByMemberId(save_member.getMemberId());
			memberOne.setMemberPw("*****");
			memberOne.setMemberPhoneNum("***-****-****");
			memberOne.setMemberEmail("****@****.***");

			// member데이터를 세션에 저장

			session.setAttribute("user", memberOne);

			return "redirect:/home";
		}

	}

	// 구글===============================================
	@Value("${google.client_id}")
	private String google_client_id;
	@Value("${google.client_secret}")
	private String google_client_secret;
	@Value("${google.redirect_uri}")
	private String google_redirect_uri;

	/*
	 * 클라이언트id와redirectURi를 넣어서 리다이렉트설정
	 */

	@GetMapping("/oauth/google")
	public String redirectToGoogle() {

		// redirect
		StringBuffer url = new StringBuffer();

		url.append("https://accounts.google.com/o/oauth2/auth?");
		url.append("client_id=" + google_client_id);
		url.append("&redirect_uri=" + google_redirect_uri);
		url.append(
				"&response_type=code&scope=https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile");

		return "redirect:" + url.toString();
	}

	@RequestMapping(value = "/api/google/callback")
	public String handleGoogleCallback(@RequestParam("code") String code, Model model, Member member,
			HttpServletRequest request, HttpSession session) {
		System.out.println("====================");
		// Step 2: Authorization Code를 Access Token으로 교환
		String accessToken = socialService.getGoogleAccessToken(code, google_client_id, google_client_secret,
				google_redirect_uri);
		// Step 3: Access Token을 사용하여 사용자 정보 가져오기
		HashMap<String, Object> userInfo = socialService.getGoogleUserInfo(accessToken);

		// 구글 - 받은 정보로 비회원이라면 회원가입
		if (userInfo == null) {
			return "redirect:/login";
		} else {
			System.out.println("============================");
			System.out.println("login info :" + userInfo.toString());
			System.out.println(userInfo.get("name"));

			member.setMemberId((String) userInfo.get("id"));
			member.setMemberName((String) userInfo.get("name"));
			member.setMemberNickname((String) userInfo.get("name"));
			member.setMemberEmail((String) userInfo.get("email"));
			member.setMemberSocialNickname("");
			// 구글 회원정보 데이터베이스 넣기!
			Member save_member = socialService.googleSignUp(member);
			Member memberOne = socialService.findByMemberId(save_member.getMemberId());
			memberOne.setMemberPw("*****");
			memberOne.setMemberPhoneNum("***-****-****");
			memberOne.setMemberEmail("****@****.***");

			// member데이터를 세션에 저장

			session.setAttribute("user", memberOne);
			return "redirect:/home";
		}
	}

}
