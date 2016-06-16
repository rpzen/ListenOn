package net.musicrecommend.www.recommend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.musicrecommend.www.music.ArtistService;
import net.musicrecommend.www.operation.OperationService;
import net.musicrecommend.www.user.UserService;
import net.musicrecommend.www.util.MashupPicker;
import net.musicrecommend.www.util.PageNation;
import net.musicrecommend.www.vo.AlbumVO;
import net.musicrecommend.www.vo.ArtistVO;
import net.musicrecommend.www.vo.DetailChartVO;
import net.musicrecommend.www.vo.KeywordVO;
import net.musicrecommend.www.vo.RecommendUserVO;
import net.musicrecommend.www.vo.SongVO;

@Controller
@RequestMapping("/")
public class RecommendController {
	private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

	@Autowired
	RecommendService recommendService;
	@Autowired
	UserService userService;
	@Autowired
	OperationService operationService;
	@Autowired
	MashupPicker mashupPicker;
	@Autowired
	ArtistService artistService;

	@RequestMapping(value = "recommend", method = RequestMethod.POST, produces = { "Application/plain; charset=UTF-8" })
	@ResponseBody
	public String recom(@ModelAttribute RecommendVO recommendVO, HttpSession session, String album_name,
			String artist_name, String song_name) throws Exception {
		String user_id = session.getAttribute("user_id").toString();
		System.out.println(recommendVO.toString());
		if (user_id == null) { // 로그인 안했을때
			return "로그인 필요";
		}
		String msg = "";
		recommendVO.setUser_no(userService.getUserNo(user_id));
		try {
			recommendService.insertRecommend(recommendVO);

			// 앨범정보랑 아티스트정보도 입력하자
			ArtistVO artistVO = mashupPicker.getArtist(recommendVO.getArtist_key(), artist_name, song_name);
			logger.info("키워드 " + artist_name + " " + song_name + "로 입력할 아티스트 : " + artistVO);
			if (artistVO == null)
				artistVO = mashupPicker.getArtist(recommendVO.getArtist_key(), artist_name);
			if (artistVO != null) {
				operationService.insertArtist(artistVO);
			} else {
				msg = "평가 완료!";
			}

			// logger.info(album_name,artist_name);
			AlbumVO albumVO = mashupPicker.getAlbumByTwoName(recommendVO.getAlbum_key(), artist_name, album_name);
			if (albumVO != null) {
				operationService.insertAlbum(albumVO);
			} else { // 두번째 방법
				albumVO = mashupPicker.getAlbumByArtistKey(recommendVO.getAlbum_key(), artist_name);
				if (albumVO == null) {
					return "평가 완료!";
				}
				operationService.insertAlbum(albumVO);
			}
			msg = "평가 완료!";
		} catch (Exception e) {
			msg = "오류";
			e.printStackTrace();
		}
		return msg;
	}

	@RequestMapping(value = "recommend/user/{user_id}", method = RequestMethod.GET, produces = {
			"Application/plain; charset=UTF-8" })
	@ResponseBody
	public List<RecommendVO> getUserRecommendList(@PathVariable String user_id) throws Exception {
		long user_no = userService.getUserNo(user_id);
		return recommendService.getUserRecommendList(user_no);
	}

	@RequestMapping(value = "recommendation/{user_id}", method = RequestMethod.GET)
	public String getRecommendation(@PathVariable String user_id) throws Exception {
		return "recommend/recommend";
	}

	// @RequestMapping(value="comment")
	// @ResponseBody
	// public List<CommentVO> comment(){
	//
	// List<CommentVO>
	//
	// }

	@RequestMapping(value = "/recommendation/song/insert", method = RequestMethod.POST)
	public String redirectToSong() {
		return "redirect:/operation/song/insert";
	}

	@RequestMapping(value = "recommend/detail/insert_comment", method = RequestMethod.POST, produces = {
			"Application/plain; charset=UTF-8" })
	@ResponseBody
	public String insert_comment(@ModelAttribute RecommendVO recommendVO, HttpSession session) {
		String msg = "";
		System.out.println("recommendVO : " + recommendVO.toString());
		try {
			String user_id = session.getAttribute("user_id").toString();
			long user_no = userService.getUserNo(user_id);
			recommendVO.setUser_no(user_no);
			msg = recommendService.insertComment(recommendVO);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("msg : " + msg);
		return msg;
	}

	@RequestMapping(value = "nullalbum", method = RequestMethod.GET, produces = { "Application/plain; charset=UTF-8" })
	public String nullalbum() throws Exception {
		List<SongVO> nullSongs = recommendService.getNullalbmSongs();
		for (SongVO songVO : nullSongs) {
			Map<String, Object> map = mashupPicker.getSongList(1, 50,
					songVO.getArtist_name() + " " + songVO.getSong_name());
			for (SongVO getVO : (List<SongVO>) (map.get("list"))) {
				if (getVO.getSong_key() == songVO.getSong_key()) {
					songVO.setAlbum_name(getVO.getAlbum_name());
					recommendService.insertNullalbum(songVO);
				}
			}
		}
		return "";
	}
	

	
	@RequestMapping(value="recommend/detail/get_comment/{pg}", method=RequestMethod.POST, produces={"Application/json; charset=UTF-8"})
	@ResponseBody
	public Map<String, Object> getComment(@ModelAttribute RecommendVO recommendVO, @PathVariable long pg){
		List<RecommendUserVO> commentList = new ArrayList<RecommendUserVO>();
		
		Map<String, Object> mapData = null;
		try {
			long commentCount = recommendService.getTotalCommentCount(recommendVO);
			PageNation pgNation = new PageNation(pg, 3, 10, commentCount);
			mapData = new HashMap<String, Object>();
			
			mapData.put("recommendVO", recommendVO);
			mapData.put("pgNation", pgNation);
			
			commentList = recommendService.getCommentList(mapData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mapData.put("commentList", commentList);
		
		return mapData;
	}
	
	@RequestMapping(value = "recommend/detail/get_chart_info", method = RequestMethod.POST, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<DetailChartVO> getChart(@ModelAttribute RecommendVO recommendVO) {

		List<DetailChartVO> detailChartVO = null;

		try {

			detailChartVO = recommendService.getStarPointSubtotal(recommendVO);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return detailChartVO;

	}

	@RequestMapping(value = "recommend/detail/get_chart_avg", method = RequestMethod.POST, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<DetailChartVO> getChartAvg(@ModelAttribute RecommendVO recommendVO) {

		List<DetailChartVO> detailChartVO = null;

		try {

			detailChartVO = recommendService.getStarPointAvg(recommendVO);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return detailChartVO;

	}

	@RequestMapping(value = "main", method = RequestMethod.GET)
	public void main() throws Exception {
		
	}

	// 메인페이지 1단 추천목록
	@RequestMapping(value = "song/mainpagelist", method = RequestMethod.GET, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<Object> getMainPageList() throws Exception {
		return recommendService.getMainPageList();
	}

	// 메인페이지 2단 90년대 히트송
	@RequestMapping(value = "song/xcenturysongs", method = RequestMethod.GET, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<SongVO> getXCenturySongs(@RequestParam("year") String year) throws Exception {
		return recommendService.getXCenturySongs(year);
	}

	// 메인페이지 3단 추천목록 (로그인체크)
	@RequestMapping(value = "song/mainpagedownlist", method = RequestMethod.GET, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<ArtistVO> getMainPageDownList(HttpSession session) throws Exception {
		if (session.getAttribute("user_id") == null) {
			return recommendService.getMainPageDownList();
		} else {
			return recommendService.getMainPageDownList(session.getAttribute("user_id").toString());
		}
	}

	// 메인페이지 4단 19
	@RequestMapping(value = "song/adultsonglist", method = RequestMethod.GET, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<SongVO> getAdultSongList() throws Exception {
		return recommendService.getAdultSongList();
	}

	// 검색어 순위
	@RequestMapping(value = "searchKeyword", method = RequestMethod.GET, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<KeywordVO> getSearchKeywordList() throws Exception {
		return recommendService.getSearchKeywordList();
	}

	// 평점수 순위
	@RequestMapping(value = "recommendrank", method = RequestMethod.GET, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public List<Map<String, Object>> getRecommendRank() throws Exception {
		return recommendService.getRecommendRank();
	}

	// 아티스트 정보가져오기
	@RequestMapping(value = "recommend/detail/get_artist_info", method = RequestMethod.POST, produces = {
			"Application/json; charset=UTF-8" })
	@ResponseBody
	public ArtistVO getArtistInfo(@ModelAttribute ArtistVO artistVO) {

		try {
			artistVO = artistService.getArtistInfo(artistVO);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return artistVO;

	}

	// 권우 추천 페이지\\
	@RequestMapping(value = "recommend/main", method = RequestMethod.GET)
	public void recommendMain(HttpSession session, Model model) {
		long user_no;
		try {
			user_no = userService.getUserNo(session.getAttribute("user_id").toString());
			long user_recommend_total_count = recommendService.getUserRecommendTotalCount(user_no);
			model.addAttribute("user_recommend_total_count", user_recommend_total_count);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
			
	}



	// 대한민국 가수\\
	@RequestMapping(value = "recommend/korea", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> korea(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.koreaCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.koreaList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("대한민국 리스트 : " + list.toString());

		return mapData;

	}

	// 아메리카 가수\\
	@RequestMapping(value = "recommend/america", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> america(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.americaCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.americaList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("아메리카 리스트 : " + list.toString());

		return mapData;

	}

	// 아시아 가수\\
	@RequestMapping(value = "recommend/asia", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> asia(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.asiaCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.asiaList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("아시아 리스트 : " + list.toString());

		return mapData;

	}

	// 유럽 가수\\
	@RequestMapping(value = "recommend/europe", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> europe(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.europeCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.europeList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("유럽 리스트 : " + list.toString());

		return mapData;

	}

	// 걸그룹\\
	@RequestMapping(value = "recommend/group_girl", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> group_girl(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.group_girlCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.group_girlList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("걸그룹 리스트 : " + list.toString());

		return mapData;

	}

	// 보이그룹\\
	@RequestMapping(value = "recommend/group_boy", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> group_boy(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.group_boyCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.group_boyList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("보이그룹 리스트 : " + list.toString());

		return mapData;

	}

	// 혼성그룹\\
	@RequestMapping(value = "recommend/group_hybrid", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> group_hybrid(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.group_hybridCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.group_hybridList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("혼성그룹 리스트 : " + list.toString());

		return mapData;

	}

	// 여성 솔로\\
	@RequestMapping(value = "recommend/solo_girl", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> solo_girl(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.solo_girlCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.solo_girlList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("여성솔로 리스트 : " + list.toString());

		return mapData;

	}

	// 남성 솔로\\
	@RequestMapping(value = "recommend/solo_boy", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> solo_boy(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.solo_boyCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.solo_boyList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("남성 솔로 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-거짓말\\
	@RequestMapping(value = "recommend/keyword_lie", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_lie(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_lieCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_lieList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("거짓말 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-리메이크\\
	@RequestMapping(value = "recommend/keyword_Remake", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_Remake(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_RemakeCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_RemakeList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("리메이크 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-리믹스\\
	@RequestMapping(value = "recommend/keyword_Remix", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_Remix(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_RemixCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_RemixList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("리믹스 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-믿음\\
	@RequestMapping(value = "recommend/keyword_trust", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_trust(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_trustCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_trustList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("믿음 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-사랑\\
	@RequestMapping(value = "recommend/keyword_love", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_love(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_loveCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_loveList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("사랑 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-생일\\
	@RequestMapping(value = "recommend/keyword_birthday", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_birthday(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_birthdayCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_birthdayList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("생일 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-소원\\
	@RequestMapping(value = "recommend/keyword_wish", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_wish(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_wishCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_wishList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("소원 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-시간\\
	@RequestMapping(value = "recommend/keyword_time", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_time(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_timeCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_timeList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("시간 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-이별\\
	@RequestMapping(value = "recommend/keyword_parting", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_parting(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_partingCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_partingList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("이별 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-이야기\\
	@RequestMapping(value = "recommend/keyword_story", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_story(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_storyCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_storyList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("이야기 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-친구\\
	@RequestMapping(value = "recommend/keyword_friend", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_friend(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_friendCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_friendList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("친구 리스트 : " + list.toString());

		return mapData;

	}

	// 키워드-톡\\
	@RequestMapping(value = "recommend/keyword_talk", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> keyword_talk(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.keyword_talkCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.keyword_talkList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("친구 리스트 : " + list.toString());

		return mapData;

	}

	// 예상별점 높음\\
	@RequestMapping(value = "recommend/high_starpoint", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> high_starpoint(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.high_starpointCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.high_starpointList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("예상별점↗ 리스트 : " + list.toString());

		return mapData;

	}

	// 선호 가수\\
	@RequestMapping(value = "recommend/favorite_singer", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> favorite_singer(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.favorite_singerCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.favorite_singerList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("선호가수 리스트 : " + list.toString());

		return mapData;
		
	}

	// 선호 출시연도\\
	@RequestMapping(value = "recommend/favorite_issue_date", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> favorite_issue_date(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.favorite_issue_dateCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.favorite_issue_dateList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("선호출시연도 리스트 : " + list.toString());

		return mapData;

	}

	// 선호 플레이타임\\
	@RequestMapping(value = "recommend/favorite_play_time", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> favorite_play_time(HttpSession session, @RequestParam int page) throws Exception {

		String user_id = (String) session.getAttribute("user_id");
		Map<String, Object> mapData = new HashMap<String, Object>();

		long totalCount = recommendService.favorite_play_timeCount(user_id);
		PageNation pgNation = new PageNation(page, 21, 21, totalCount);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("start_num", pgNation.getStart_num());
		map.put("end_num", pgNation.getEnd_num());

		List<SongVO> list = recommendService.favorite_play_timeList(map);

		mapData.put("list", list);
		mapData.put("pgNation", pgNation);

		logger.info("선호플레이타임 리스트 : " + list.toString());

		return mapData;

	}

	//메인페이지 5단 서바이벌 프로그램
		@RequestMapping(value="song/survivalsonglist", method=RequestMethod.GET, produces={"Application/json; charset=UTF-8"})
		@ResponseBody
		public List<SongVO> getSurvivalSongList() throws Exception{
			return recommendService.getSurvivalSongList();
		}
	
}
