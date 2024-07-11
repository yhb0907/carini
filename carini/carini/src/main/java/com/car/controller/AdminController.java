package com.car.controller;

import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import com.car.dto.Board;
import com.car.dto.Bookmark;
import com.car.dto.Car;
import com.car.dto.Inquiry;
import com.car.dto.Member;
import com.car.dto.Notice;
import com.car.dto.PagingInfo;
import com.car.service.BoardService;
import com.car.service.BookMarkService;
import com.car.service.CommentService;
import com.car.service.InquiryService;
import com.car.service.MemberService;
import com.car.service.ModelService;
import com.car.service.NoticeService;
import com.car.validation.AdminMemberValidation;
import com.car.validation.BoardUpdateFormValidation;
import com.car.validation.BoardWriteFormValidation;
import com.car.validation.NoticeUpdateFormValidation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
   
   private final BoardService boardService;
   private final MemberService memberService;
   private final NoticeService noticeService;
   private final InquiryService inquiryService;
   private final ModelService modelService;
   private final BookMarkService bookmarkService;
   private final MessageSource messageSource;
   private final LocaleResolver localeResolver;

   
   public PagingInfo pagingInfo = new PagingInfo();
   
   @Value("${path.upload}")
   public String uploadFolder;
   
   @ModelAttribute("member")
   public Member setMember() {
      return new Member(); // 기본 Member 객체를 세션에 저장
   }
   
   @GetMapping("/adminList")
   public String adminList(Model model) {
      
      return "admin/adminList";
   }
   
   /*
    * 회원(Member) 관리 =======================================================================
    * */
   
   @GetMapping("/memberList")
   public String showMemberList(Model model, Member member,
       @RequestParam(name = "curPage", defaultValue = "0") int curPage,
       @RequestParam(name = "rowSizePerPage", defaultValue = "10") int rowSizePerPage,
       @RequestParam(name = "searchType", defaultValue = "memberNickname") String searchType,
       @RequestParam(name = "searchWord", defaultValue = "") String searchWord) {
      
       curPage = Math.max(curPage, 0);  // Ensure curPage is not negative
       Pageable pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("memberDate").descending());
       Page<Member> pagedResult = memberService.getMemberList(pageable, searchType, searchWord);
       List<Member> memberList = pagedResult.getContent();
       Map<String, List<Integer>> memberCountMap = new HashMap<>();
       
       for (Member member1 : memberList) {
           int boardCount = boardService.countBoardById(member1.getMemberId());
           int bookmarkCount = bookmarkService.countBookmarkById(member1.getMemberId());
           int inquiryCount = inquiryService.countInquiryById(member1.getMemberId());
           List<Integer> counts = Arrays.asList(boardCount, bookmarkCount, inquiryCount);
           memberCountMap.put(member1.getMemberId(), counts);
       }
       
       int totalRowCount  = (int)pagedResult.getNumberOfElements();
       int totalPageCount = pagedResult.getTotalPages();
       int pageSize       = pagingInfo.getPageSize();
       int startPage      = (curPage / pageSize) * pageSize + 1;
       int endPage        = startPage + pageSize - 1;
       endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;
       
   
       pagingInfo.setCurPage(curPage);
       pagingInfo.setTotalRowCount(totalRowCount);
       pagingInfo.setTotalPageCount(totalPageCount);
       pagingInfo.setStartPage(startPage);
       pagingInfo.setEndPage(endPage);
       pagingInfo.setSearchType(searchType);
       pagingInfo.setSearchWord(searchWord); 
       pagingInfo.setRowSizePerPage(rowSizePerPage);
       
       model.addAttribute("pagingInfo", pagingInfo);
       model.addAttribute("pagedResult", pagedResult);
       model.addAttribute("pageable", pageable);
       model.addAttribute("cp", curPage);
       model.addAttribute("sp", startPage);
       model.addAttribute("ep", endPage);
       model.addAttribute("ps", pageSize);
       model.addAttribute("rp", rowSizePerPage);
       model.addAttribute("tp", totalPageCount);
       model.addAttribute("st", searchType);
       model.addAttribute("sw", searchWord);
       model.addAttribute("memberList", pagedResult.getContent());
       model.addAttribute("memberCountMap", memberCountMap);
   
       return "admin/memberList";
   }
   
   @GetMapping("/bookmarkList")
   public String myPagebookmarkList(@RequestParam("memberId") String memberId, Model model, HttpServletRequest request, HttpSession session) {
      Locale locale = localeResolver.resolveLocale(request);

      List<Bookmark> bookmarkCarID = bookmarkService.findAllBookmarkCar(memberId);

      List<Car> bookmarkCarList = bookmarkService.findAllCar(bookmarkCarID);
      
      model.addAttribute("memberId", memberId);
      model.addAttribute("BookmarkCarList", bookmarkCarList);
      return "admin/bookmarkList";
   }
   
   @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

   @GetMapping("/updateMember")
   public String updateMemberForm(@RequestParam("memberId") String memberId, Model model) {
      
      Member findmember = memberService.findByMemberId(memberId);
      
      model.addAttribute("member", findmember);
      
      
      return "admin/updateMember";
   }
   
   @PostMapping("/updateMember")
   public String updateMember(@ModelAttribute("member") Member member) {
      
      memberService.updateAllMember(member.getMemberId(), member);
      
      return "admin/updateMember";
   }
   
   @GetMapping("/deleteMember")
   public String deleteMember(@RequestParam("memberId") String memberId, Model model, HttpServletRequest request) {
      
      Member member = memberService.findByMemberId(memberId);
      Locale locale = localeResolver.resolveLocale(request);
      memberService.deleteMember(member);

      model.addAttribute("msg", messageSource.getMessage("info.exit.success", null, locale));
      model.addAttribute("url", "/");
      
      return "alert";
   }
   
   @GetMapping("/insertMember")
   public String insertMemberForm(@ModelAttribute("AdminMemberValidation") AdminMemberValidation member) {
      return "admin/insertMember";
   }
   
   @PostMapping("/insertMember")
   public String insertMember(@Validated @ModelAttribute("AdminMemberValidation") AdminMemberValidation member,BindingResult bindingResult,Model model) {
         
      if(bindingResult.hasErrors()) {
         return "admin/insertMember";
      }
      
      
      List<Member> findmemberEmail=memberService.findByMemberEmail(member.getMemberEmail());
      List<Member> findmemberNickname=memberService.findByMemberNickname(member.getMemberNickname());
      Member findmemberId=memberService.findByMemberId(member.getMemberId());
      List<Member> findmemberPhone = memberService.findByMemberPhoneNum(member.getMemberPhoneNum());
      
      // 아이디 중복검사
      if(findmemberId != null && findmemberId.getMemberId().equals(member.getMemberId())) {
         bindingResult.rejectValue("memberId", null, "존재하는 아이디입니다."); 
         return "member/signup";
      }

      // 닉네임 중복검사

      if(!findmemberNickname.isEmpty()) {
         bindingResult.rejectValue("memberNickname",null, "존재하는 닉네임입니다."); 
         return "member/signup";
      }
      
      // 이메일 중복검사
      if(!findmemberEmail.isEmpty()) {
         bindingResult.rejectValue("memberEmail",null, "존재하는 이메일입니다."); 
         return "member/signup";
      }
      
      // 전화번호 중복검사
      if(!findmemberPhone.isEmpty()) {
         bindingResult.rejectValue("memberPhoneNum", null, "존재하는 전화번호입니다"); 
         return "member/signup";
      }
      
      Member Member = new Member();
      Member.setMemberEmail(member.getMemberEmail());
      Member.setMemberId(member.getMemberId());
      Member.setMemberPw(member.getMemberPw());
      Member.setMemberName(member.getMemberName());
      Member.setMemberNickname(member.getMemberNickname());
      Member.setMemberPhoneNum(member.getMemberPhoneNum());
      Member.setMemberSocial(member.getMemberSocial());
      Member.setMemberRole(member.getMemberRole());
         
      Member save_member=memberService.insertMember(Member);
         
      model.addAttribute("msg", "성공적으로 회원가입이 되었습니다.");
        model.addAttribute("url", "/admin/insertMember");   
        return "alert";   
               
   }
   
   /*
    * 게시판(Board) 관리 =========================================================================
    * */
   
   // 게시판 조회
   @GetMapping("/boardList")
   public String getBoardList(Model model, Board board,
          @RequestParam(name = "curPage", defaultValue = "0") int curPage,
          @RequestParam(name = "rowSizePerPage", defaultValue = "10") int rowSizePerPage,
          @RequestParam(name = "searchType", defaultValue = "boardWriter") String searchType,
          @RequestParam(name = "searchWord", defaultValue = "") String searchWord,
          @RequestParam(name = "boardSort", defaultValue = "번호순") String boardSort) {
      
       curPage = Math.max(curPage, 0);  // Ensure curPage is not negative
       Pageable pageable;
       
       if(boardSort.equals("번호순")){
         pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("boardId").ascending());
      }else if(boardSort.equals("조회순")) {
         pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("boardCnt").descending());
      }else if(boardSort.equals("오래된순")){
         pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("boardDate").ascending());
      }else {
         pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("boardDate").descending());
      }
       
       Page<Board> pagedResult = boardService.getBoardList(pageable, searchType, searchWord);
       
       int totalRowCount  = (int)pagedResult.getNumberOfElements();
       int totalPageCount = pagedResult.getTotalPages();
       int pageSize       = pagingInfo.getPageSize();
       int startPage      = (curPage / pageSize) * pageSize + 1;
       int endPage        = startPage + pageSize - 1;
       endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;
       
   
       pagingInfo.setCurPage(curPage);
       pagingInfo.setTotalRowCount(totalRowCount);
       pagingInfo.setTotalPageCount(totalPageCount);
       pagingInfo.setStartPage(startPage);
       pagingInfo.setEndPage(endPage);
       pagingInfo.setSearchType(searchType);
       pagingInfo.setSearchWord(searchWord); 
       pagingInfo.setRowSizePerPage(rowSizePerPage);
       
       model.addAttribute("pagingInfo", pagingInfo);
       model.addAttribute("pagedResult", pagedResult);
       model.addAttribute("pageable", pageable);
       model.addAttribute("cp", curPage);
       model.addAttribute("sp", startPage);
       model.addAttribute("ep", endPage);
       model.addAttribute("ps", pageSize);
       model.addAttribute("rp", rowSizePerPage);
       model.addAttribute("tp", totalPageCount);
       model.addAttribute("st", searchType);
       model.addAttribute("sw", searchWord);
       model.addAttribute("bs", boardSort);
       model.addAttribute("boardList", pagedResult.getContent()); // Add this line

       return "admin/boardList";
   }
   
   // 게시판 수정 폼
   @GetMapping("/updateBoard")
   public String updateBoardForm(@RequestParam("boardId") Long boardId, Model model,
                                 @ModelAttribute("BoardUpdateFormValidation") BoardUpdateFormValidation boardValidation,
                                 BindingResult bindingResult) {
       
       Board board = boardService.getBoardById(boardId);
       if (board == null) {
           model.addAttribute("msg", "게시글을 찾을 수 없습니다.");
           model.addAttribute("url", "/admin/boardList");
           return "alert";
       }

        boardValidation.setBoardTitle(board.getBoardTitle());
        boardValidation.setBoardContent(board.getBoardContent());
        
        model.addAttribute("BoardUpdateFormValidation", boardValidation);
        model.addAttribute("board", board);
        
        return "admin/updateBoard";  // 게시글 수정 페이지

   }
   
   // 게시판 수정
   @PostMapping("/updateBoard")
   public String updateBoard(Board board,@Validated @ModelAttribute("BoardUpdateFormValidation") BoardUpdateFormValidation boardValidation ,
            BindingResult bindingResult, Model model)  {
         
         if (bindingResult.hasErrors()) {
   
             return "admin/updateBoard";
          }
      
      // 파일재업로드
      MultipartFile uploadFile = board.getUploadFile();
      if(uploadFile != null && !uploadFile.isEmpty()) {
         String fileName = uploadFile.getOriginalFilename();
         Path filePath = Paths.get(uploadFolder + fileName);
         try {
            Files.copy(uploadFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            board.setBoardFilename(fileName);
         } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
         }         
      }
      boardService.updateBoard(board);
      model.addAttribute("msg", "게시글이 수정되었습니다!");
      model.addAttribute("url", "/admin/boardList");
      return "alert";
   }
   
   @PostMapping("/deleteBoard")
   @ResponseBody
   public Map<String, Object> deleteBoard(@RequestParam("boardId") Long boardId) {
       Map<String, Object> response = new HashMap<>();
       try {
           Board findboard = boardService.getBoardById(boardId);
           if(findboard == null) {
               response.put("success", false);
               response.put("message", "해당 게시물이 존재하지 않습니다.");
           } else {
               boardService.deleteBoard(findboard);
               response.put("success", true);
               response.put("message", "해당 게시물을 삭제하였습니다.");
           }
       } catch (Exception e) {
           response.put("success", false);
           response.put("message", "게시물 삭제 중 오류가 발생했습니다.");
       }
       return response;
   }
   
   
   /*
    * 공지사항(Notice) 관리 =======================================================================
   * */
   
   // 공지사항 목록보기
   @GetMapping("/noticeList")
   public String getNoticeList(Model model, Notice notice,
          @RequestParam(name = "curPage", defaultValue = "0") int curPage,
          @RequestParam(name = "rowSizePerPage", defaultValue = "10") int rowSizePerPage,
          @RequestParam(name = "searchType", defaultValue = "noticeTitle") String searchType,
          @RequestParam(name = "searchWord", defaultValue = "") String searchWord) {
      
      curPage = Math.max(curPage, 0);  // Ensure curPage is not negative
       Pageable pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("noticeId").descending());
       Page<Notice> pagedResult = noticeService.getNoticeList(pageable, searchType, searchWord);
       
       int totalRowCount  = (int)pagedResult.getNumberOfElements();
       int totalPageCount = pagedResult.getTotalPages();
       int pageSize       = pagingInfo.getPageSize();
       int startPage      = (curPage / pageSize) * pageSize + 1;
       int endPage        = startPage + pageSize - 1;
       endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;
       

       pagingInfo.setCurPage(curPage);
       pagingInfo.setTotalRowCount(totalRowCount);
       pagingInfo.setTotalPageCount(totalPageCount);
       pagingInfo.setStartPage(startPage);
       pagingInfo.setEndPage(endPage);
       pagingInfo.setSearchType(searchType);
       pagingInfo.setSearchWord(searchWord); 
       pagingInfo.setRowSizePerPage(rowSizePerPage);
       
       model.addAttribute("pagingInfo", pagingInfo);
       model.addAttribute("pagedResult", pagedResult);
       model.addAttribute("pageable", pageable);
      model.addAttribute("cp", curPage);
      model.addAttribute("sp", startPage);
      model.addAttribute("ep", endPage);
      model.addAttribute("ps", pageSize);
      model.addAttribute("rp", rowSizePerPage);
      model.addAttribute("tp", totalPageCount);
      model.addAttribute("st", searchType);
      model.addAttribute("sw", searchWord);
       model.addAttribute("noticeList", pagedResult.getContent()); // Add this line

       return "admin/noticeList";
   }
   
   // 공지사항 수정 폼
   @GetMapping("/updateNotice")
   public String updateNoticeForm(@RequestParam("noticeId") Long noticeId, Model model,
         @ModelAttribute("NoticeUpdateFormValidation") NoticeUpdateFormValidation noticeValidation,
         BindingResult bindingResult) {

      Notice notice = noticeService.getNoticebyId(noticeId);
      
      if(notice == null) {
         model.addAttribute("msg", "게시글을 찾을 수 없습니다.");
           model.addAttribute("url", "/notice/getNoticeList");
           return "alert";
      }
      // user의 Role이 ROLE_ADMIN일 때만 업데이트할 수 있도록 처리
      noticeValidation.setNoticeTitle(notice.getNoticeTitle());
      noticeValidation.setNoticeContent(notice.getNoticeContent());
      
      model.addAttribute("NoticeUpdateFormValidation", noticeValidation);
      model.addAttribute("notice", notice);
      
      return "admin/updateNotice";
   

   }
   
   // 공지사항 수정
   @PostMapping("/updateNotice")
   public String updateNotice(Notice notice, @Validated @ModelAttribute("NoticeUpdateFormValidation") NoticeUpdateFormValidation noticeValidation,
         BindingResult bindingResult, Model model) {
      
      if (bindingResult.hasErrors()) {
         return "admin/updateNotice";
      }
      
      // 파일재업로드
      MultipartFile uploadFile = notice.getUploadFile();
      if(uploadFile != null && !uploadFile.isEmpty()) {
         String fileName = uploadFile.getOriginalFilename();
         Path filePath = Paths.get(uploadFolder + fileName);
         try {
            Files.copy(uploadFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            notice.setNoticeFilename(fileName);
         } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
         }         
      }
      
      noticeService.updateNotice(notice);
      model.addAttribute("msg", "공지사항이 수정되었습니다!");
      model.addAttribute("url", "/notice/getNoticeList");
      return "alert";
   }
   
   // 공지사항 작성 폼
   @GetMapping("/insertNotice")
   public String insertNoticeForm(Notice notice, Model model) {
      LocalDate currentDate = LocalDate.now();
      
      model.addAttribute("NoticeUpdateFormValidation", new NoticeUpdateFormValidation());
      model.addAttribute("date",currentDate);
      return "admin/insertNotice";

      
   }

   // 공지사항 작성
   @PostMapping("/insertNotice")
   public String insertNotice(Notice notice,@Validated @ModelAttribute("NoticeUpdateFormValidation") NoticeUpdateFormValidation noticeValidation,
            BindingResult bindingResult, Model model) throws IOException {
      
        LocalDate currentDate = LocalDate.now();
        model.addAttribute("date",currentDate);
      if(bindingResult.hasErrors()) {
            return "admin/insertNotice";
      }
   
      // 파일업로드
      MultipartFile uploadFile = notice.getUploadFile();
      if(!uploadFile.isEmpty()) {
         String fileName = uploadFile.getOriginalFilename();
         
         uploadFile.transferTo(new File(uploadFolder + fileName));
         notice.setNoticeFilename(fileName);
      }
      
      noticeService.insertNotice(notice);
      
      model.addAttribute("msg", "공지사항이 작성되었습니다!");
      model.addAttribute("url", "/notice/getNoticeList");
      return "alert";
   }
   
   // 공지사항 파일 다운로드
   @GetMapping("/notice/download")
   public ResponseEntity<Resource> handleFileDownload(HttpServletRequest req, 
         @RequestParam(name = "noticeId") Long noticeId, @RequestParam(name = "fn") String fn) throws Exception {
         req.setCharacterEncoding("utf-8");
         String fileName = req.getParameter("fn");
       Path filePath = Paths.get(uploadFolder + fileName).toAbsolutePath();
       Resource resource = null;
       try {
           resource = new UrlResource(filePath.toUri());           
           if (resource.exists()) {
               return ResponseEntity.ok()
                       .contentType(MediaType.APPLICATION_OCTET_STREAM)
                       .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" 
                             + URLEncoder.encode(resource.getFilename(), "utf-8") + "\"")
                       .body(resource);
           } else {
               return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
           }
       } catch (MalformedURLException e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }
   }
   
   // 공지사항 파일 삭제
   @PostMapping("/notice/deleteFile/{noticeId}")
   @ResponseBody
   public ResponseEntity<Map<String, String>> deleteFile(@PathVariable(name = "noticeId") Long noticeId, HttpServletRequest request) {
       Map<String, String> response = new HashMap<>();
       Locale locale = localeResolver.resolveLocale(request);
       try {
          
           noticeService.deleteFile(noticeId);
           response.put("message", messageSource.getMessage("board.filedelete.success", null, locale));
           response.put("status", "success");
           return ResponseEntity.ok(response);
       } catch (Exception e) {
   
           log.error("게시글 파일 삭제 중 오류 발생: {}", e.getMessage(), e);
           response.put("message", messageSource.getMessage("board.filedelete.failure", null, locale));
           response.put("status", "error");
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
           
          //throw new BoardDeleteFileException(ErrorCode.BOARD_DELETE,null);
       }
   }
   
   // 공지사항 삭제
   @GetMapping("deleteNotice")
   public String deleteNotice(Notice notice, Model model)  {
     Notice findNotice = noticeService.getNoticebyId(notice.getNoticeId());
     
     if(findNotice ==null) {
        model.addAttribute("msg", "해당 공지사항이 존재하지않습니다.");
        model.addAttribute("url", "/notice/getNoticeList");
        return "alert";
     }
     
     noticeService.deleteNoticeById(findNotice.getNoticeId());
     model.addAttribute("msg", "해당 공지사항을 삭제하였숩니다.");
     model.addAttribute("url", "/notice/getNoticeList");
     return "alert";
   }
   
   /*
    * 문의(inquiry) 관리 =======================================================================
   * */
   
   // 문의함 리스트
   @GetMapping("inquiryList")
   public String getInquiryList(Model model, Inquiry inquiry,
             @RequestParam(name = "curPage", defaultValue = "0") int curPage,
             @RequestParam(name = "rowSizePerPage", defaultValue = "10") int rowSizePerPage,
             @RequestParam(name = "searchType", defaultValue = "reTitle") String searchType,
             @RequestParam(name = "searchWord", defaultValue = "") String searchWord) {
         
      curPage = Math.max(curPage, 0);  // Ensure curPage is not negative
       Pageable pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("reDate").descending());
       Page<Inquiry> pagedResult = inquiryService.getInquiryList(pageable, searchType, searchWord);
       
       int totalRowCount  = (int)pagedResult.getNumberOfElements();
       int totalPageCount = pagedResult.getTotalPages();
       int pageSize       = pagingInfo.getPageSize();
       int startPage      = (curPage / pageSize) * pageSize + 1;
       int endPage        = startPage + pageSize - 1;
       endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;
       
       pagingInfo.setCurPage(curPage);
       pagingInfo.setTotalRowCount(totalRowCount);
       pagingInfo.setTotalPageCount(totalPageCount);
       pagingInfo.setStartPage(startPage);
       pagingInfo.setEndPage(endPage);
       pagingInfo.setSearchType(searchType);
       pagingInfo.setSearchWord(searchWord); 
       pagingInfo.setRowSizePerPage(rowSizePerPage);

       model.addAttribute("pagingInfo", pagingInfo);
       model.addAttribute("pagedResult", pagedResult);
       model.addAttribute("pageable", pageable);
      model.addAttribute("cp", curPage);
      model.addAttribute("sp", startPage);
      model.addAttribute("ep", endPage);
      model.addAttribute("ps", pageSize);
      model.addAttribute("rp", rowSizePerPage);
      model.addAttribute("tp", totalPageCount);
      model.addAttribute("st", searchType);
      model.addAttribute("sw", searchWord);
       model.addAttribute("inquiryList", pagedResult.getContent()); // Add this line
       
       return "admin/inquiryList";
   }
   
   // 답변 등록 및 수정 폼
   @GetMapping("/answerInquiry")
   public String answerInquiryForm(@RequestParam("reId") Long reId, Model model) {
      
      LocalDate currentDate = LocalDate.now();
      Inquiry findInquiry = inquiryService.findbyreIdinquiry(reId);
      
      if(findInquiry != null) {
         
         model.addAttribute("date",currentDate);
         model.addAttribute("inquiry",findInquiry);
         return "admin/answerInquiry";
         
      } else {
         model.addAttribute("msg", "문의 내역이 없습니다!");
          model.addAttribute("url", "/admin/inquiryList");
         return "alert";
         
      }
   }
   
   // 답변 등록 및 수정
   @PostMapping("/answerInquiry")
   public String answerInquiry(@ModelAttribute("inquiry") Inquiry inquiry, Model model) {

      inquiry.setReCheckRq(true);
      inquiryService.answerInquiry(inquiry);
      System.out.println("4444444444");
      System.out.println(inquiry);
      model.addAttribute("msg", "문의번호 "+ inquiry.getReId() +"번 답변완료!");
      model.addAttribute("url", "/admin/inquiryList");
      return "alert";
      
   }
   
   // 문의 삭제
   @GetMapping("/deleteInquiry")
   public String deleteInquiryForm(@RequestParam("reId") Long reId, Model model) {
      
      Inquiry findInquiry = inquiryService.findbyreIdinquiry(reId);
      
      if(findInquiry != null) {
         inquiryService.deleteInquiryById(reId);
         model.addAttribute("msg", "삭제완료!");
         model.addAttribute("url", "/admin/inquiryList");
         return "alert";
      } else {
         model.addAttribute("msg", "문의 내역이 없습니다!");
          model.addAttribute("url", "/admin/inquiryList");
         return "alert";
      } 
      
   }
   
   
   /*
    * 모델(Model) 관리 =======================================================================
   * */
   
   @GetMapping("/modelList")
   public String getBoardList(Model model, 
          @RequestParam(name = "curPage", defaultValue = "0") int curPage,
          @RequestParam(name = "rowSizePerPage", defaultValue = "10") int rowSizePerPage,
          @RequestParam(name = "filterMinPrice", defaultValue = "0") Long filterMinPrice,
          @RequestParam(name = "filterMaxPrice", defaultValue = "1000000000") Long filterMaxPrice,
          @RequestParam(name = "filterSize", defaultValue = "선택안함") String filterSize,
          @RequestParam(name = "filterFuel", defaultValue = "선택안함") String filterFuel,
          @RequestParam(name = "carSort", defaultValue = "저가순") String carSort,
          @RequestParam(name = "searchWord", defaultValue = "") String searchWord,
          @RequestParam(name = "exCar", defaultValue = "false") Boolean exCar,
          HttpSession session) {

      Member user = (Member) session.getAttribute("user");
      
      curPage = Math.max(curPage, 0);  // Ensure curPage is not negative
      
      Pageable pageable = PageRequest.of(curPage, rowSizePerPage);
       
       Page<Car> pagedResult = modelService.filterCars(pageable, filterMinPrice, filterMaxPrice, filterSize, filterFuel, searchWord, carSort, exCar);
       
       if(exCar) {
	    	filterMinPrice = 50000L;
	    	filterMaxPrice = 10000000L;
	    }
       
       int totalRowCount  = (int)pagedResult.getNumberOfElements();
       int totalPageCount = pagedResult.getTotalPages();
       int pageSize       = pagingInfo.getPageSize();
       int startPage      = (curPage / pageSize) * pageSize + 1;
       int endPage        = startPage + pageSize - 1;
       endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;
       
       pagingInfo.setCurPage(curPage);
       pagingInfo.setTotalRowCount(totalRowCount);
       pagingInfo.setTotalPageCount(totalPageCount);
       pagingInfo.setStartPage(startPage);
       pagingInfo.setEndPage(endPage);
       pagingInfo.setCarMinPrice(filterMinPrice);
       pagingInfo.setCarMaxPrice(filterMaxPrice);
       pagingInfo.setCarSize(filterSize);
       pagingInfo.setCarFuel(filterFuel);
       pagingInfo.setSearchWord(searchWord);
       pagingInfo.setRowSizePerPage(rowSizePerPage);
       pagingInfo.setExCar(exCar);
       
       model.addAttribute("pagingInfo", pagingInfo);
       model.addAttribute("pagedResult", pagedResult);
       model.addAttribute("pageable", pageable);
        model.addAttribute("cp", curPage);
        model.addAttribute("sp", startPage);
        model.addAttribute("ep", endPage);
        model.addAttribute("ps", pageSize);
        model.addAttribute("rp", rowSizePerPage);
        model.addAttribute("tp", totalPageCount);
        model.addAttribute("sw", searchWord);
        model.addAttribute("fpr", filterMinPrice);
        model.addAttribute("fph", filterMaxPrice);
        model.addAttribute("fs", filterSize);
        model.addAttribute("ff", filterFuel);
        model.addAttribute("cs", carSort);
        model.addAttribute("exCar", exCar.toString());
       model.addAttribute("carList", pagedResult.getContent());
       
       return "admin/modelList";
   }
   
   @GetMapping("/updateCar")
   public String updateCarForm(@RequestParam("carId") int carId, Model model) {
      Car car = modelService.getCarbyId(carId);
      
      if(car != null) {
         String carBrand = car.getCarName().strip().split(" ")[0];
         model.addAttribute("car", car);
         model.addAttribute("carBrand", carBrand);
         return "admin/updateCar";
      } else {
         model.addAttribute("msg", "차 데이터가 없습니다!");
          model.addAttribute("url", "/admin/modelList");
         return "alert";
      } 
      

   }
   
   @PostMapping("/updateCar")
   public String updateCar(@ModelAttribute("car") Car car, Model model) {
      System.out.println(car);
      if(car != null) {
         modelService.updateCar(car);
         return "redirect:/admin/modelList";
      } else {
         model.addAttribute("msg", "차 데이터가 없습니다!");
          model.addAttribute("url", "/admin/modelList");
         return "alert";
      } 

   }
   
   @GetMapping("insertCar")
   public String insertCarForm(Car car, Model model) {
      
      model.addAttribute("car", car);
      
      return "admin/insertCar";

      
   }
   
   @PostMapping("/insertCar")
   public String insertCar(@ModelAttribute("car") Car car, Model model) {
      
      modelService.insertCar(car);
      
      return "redirect:/admin/modelList";
      
   }
   
   @PostMapping("/deleteCar")
   public ResponseEntity<String> deleteCar(@RequestParam("carId") int carId) {
       try {
           modelService.deleteCar(carId);
           return ResponseEntity.ok("Car deleted successfully");
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting car");
       }
   }

   
}


















