package net.musicrecommend.www.recommend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.musicrecommend.www.user.UserService;
import net.musicrecommend.www.vo.AlbumVO;
import net.musicrecommend.www.vo.ArtistVO;
import net.musicrecommend.www.vo.DetailChartVO;
import net.musicrecommend.www.vo.KeywordVO;
import net.musicrecommend.www.vo.RecommendUserVO;
import net.musicrecommend.www.vo.SongVO;

@Service
public class RecommendServiceImpl implements RecommendService {
	private static final Logger logger = LoggerFactory.getLogger(RecommendServiceImpl.class);
	@Autowired
	RecommendMapper recommendMapper;
	@Autowired
	UserService userService;

	@Override
	public boolean insertRecommend(RecommendVO recommendVO) throws Exception {
		// 이미 입력된 평가가 있을시 업데이트
		if (recommendMapper.checkRecommendExist(recommendVO) > 0) {
			if (recommendMapper.updateRecommend(recommendVO) > 0) {
				return true;
			} else {
				return false;
			}
		} else {
			if (recommendMapper.insertRecommend(recommendVO) > 0) {
				return true;
			} else {
				return false;
			}

		}
	}

	@Override
	public long getTotalCount() throws Exception {
		return recommendMapper.getTotalCount();
	}

	@Override
	public List<RecommendVO> getUserRecommendList(long user_no) throws Exception {
		// 유저별 평가 목록 가져오기
		return recommendMapper.getUserRecommendList(user_no);
	}

	@Override
	public String insertComment(RecommendVO recommendVO) throws Exception {
		String msg = "";

		if (recommendMapper.checkCommentExist(recommendVO) > 0) {
			if (recommendMapper.updateUserComment(recommendVO) > 0) {
				msg = "등록 완료";
			}
		} else {
			msg = "평가를 먼저 해주세요!";
		}

		return msg;

	}

	@Override
	public List<SongVO> getNullalbmSongs() throws Exception {
		return recommendMapper.getNullalbmSongs();

	}

	@Override
	public void insertNullalbum(SongVO songVO) throws Exception {
		recommendMapper.insertNullalbum(songVO);
	}

	@Override
	public RecommendVO getUseRecommendInfo(long user_no) throws Exception {
		// TODO Auto-generated method stub
		return recommendMapper.getUseRecommendInfo(user_no);
	}

	@Override
	public List<RecommendUserVO> getCommentList(Map<String, Object> mapData) throws Exception {
			return recommendMapper.getCommentList(mapData);
		
		
	}

	@Override
	public List<DetailChartVO> getStarPointSubtotal(RecommendVO recommendVO) throws Exception {
		return recommendMapper.getStarPointSubtotal(recommendVO);
	}

	@Override
	public List<DetailChartVO> getStarPointAvg(RecommendVO recommendVO) throws Exception {

		return recommendMapper.getStarPointAvg(recommendVO);
	}

	@Override
	public List<Object> getMainPageList() throws Exception {

		List<SongVO> songlist = recommendMapper.getHighRatedSongs();
		List<AlbumVO> albumlist = recommendMapper.getHighRatedAlbums();
		List<ArtistVO> artistlist = recommendMapper.getHighRatedArtists();
		List<Object> list = new ArrayList<Object>();
		list.addAll(songlist);
		list.addAll(albumlist);
		list.addAll(artistlist);
		return list;
	}

	@Override
	public List<KeywordVO> getSearchKeywordList() throws Exception {
		return recommendMapper.getSearchKeywordList();
	}

	@Override
	public List<ArtistVO> getMainPageDownList() throws Exception {
		List<ArtistVO> resultList = new LinkedList<ArtistVO>();
		List<ArtistVO> danceList = recommendMapper.getHighRatedArtistsDefault("dance");
		List<ArtistVO> rockList = recommendMapper.getHighRatedArtistsDefault("rock");
		List<ArtistVO> balladList = recommendMapper.getHighRatedArtistsDefault("ballad");
		List<ArtistVO> dramaList = recommendMapper.getHighRatedArtistsDefault("drama");
		List<ArtistVO> rnBList = recommendMapper.getHighRatedArtistsDefault("R&B");
		List<ArtistVO> popList = recommendMapper.getHighRatedArtistsDefault("pop");
		List<ArtistVO> movieList = recommendMapper.getHighRatedArtistsDefault("movie");
		List<ArtistVO> newageList = recommendMapper.getHighRatedArtistsDefault("new age");
		List<ArtistVO> concertoList = recommendMapper.getHighRatedArtistsDefault("concerto");
		List<ArtistVO> jazzList = recommendMapper.getHighRatedArtistsDefault("jazz");
		for (int i = 0; i < 3; i++) {
			resultList.add(danceList.get(i));
			resultList.add(rockList.get(i));
			resultList.add(balladList.get(i));
			resultList.add(dramaList.get(i));
			resultList.add(rnBList.get(i));
			resultList.add(popList.get(i));
			resultList.add(movieList.get(i));
			resultList.add(newageList.get(i));
			resultList.add(concertoList.get(i));
			resultList.add(jazzList.get(i));
		}

		return resultList;
	}

	@Override
	public List<ArtistVO> getMainPageDownList(String user_id) throws Exception {
		List<ArtistVO> resultList = new LinkedList<ArtistVO>();
		List<ArtistVO> defaultList = getMainPageDownList();
		List<List<ArtistVO>> sourceLists = new ArrayList<List<ArtistVO>>();
		String[] genre_names = { "dance", "rock", "ballad", "drama", "R&B", "pop", "movie", "new age", "concerto",
				"jazz" };
		long user_no = userService.getUserNo(user_id);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("user_no", user_no);
		param.put("genre", "dance");

		for (int i = 0; i < 10; i++) {
			param.remove("genre");
			param.put("genre", genre_names[i]);
			sourceLists.add(recommendMapper.getHighRatedArtistsUser(param));
		}

		for (int i = 0; i < 10; i++) {
			for (int j = 0, k = 0; j < sourceLists.get(i).size(); j++) {
				List<ArtistVO> thisList = sourceLists.get(i);
				for (ArtistVO a : thisList) {
				}
				if (k > 2)
					break;

				boolean isContainKey = false;
				for (ArtistVO a : resultList) {
					if (a.getArtist_key() == thisList.get(j).getArtist_key()) {
						isContainKey = true;
						break;
					}
				}
				if (!isContainKey) {
					thisList.get(j).setGenre_names(genre_names[i]);
					resultList.add(thisList.get(j));
					k++;
				}
			}
		}
		return resultList;
	}

	@Override
	public List<SongVO> getXCenturySongs(String year) throws Exception {

		return recommendMapper.getXCenturySongs(year);
	}

	@Override
	public List<Map<String, Object>> getRecommendRank() throws Exception {
		return recommendMapper.getRecommendRank();
	}

	@Override
	public List<SongVO> getAdultSongList() throws Exception {
		return recommendMapper.getAdultSongList();
	}
	
	@Override
	public List<SongVO> getSurvivalSongList() throws Exception {
		return recommendMapper.getSurvivalSongList();
	}

	// 권우\\
	@Override
	public long koreaCount(String user_id) throws Exception {
		return recommendMapper.koreaCount(user_id);
	}

	@Override
	public List<SongVO> koreaList(Map<String, Object> map) throws Exception {
		return recommendMapper.koreaList(map);
	}

	@Override
	public long americaCount(String user_id) throws Exception {
		return recommendMapper.americaCount(user_id);
	}

	@Override
	public List<SongVO> americaList(Map<String, Object> map) throws Exception {
		return recommendMapper.americaList(map);
	}

	@Override
	public long asiaCount(String user_id) throws Exception {
		return recommendMapper.asiaCount(user_id);
	}

	@Override
	public List<SongVO> asiaList(Map<String, Object> map) throws Exception {
		return recommendMapper.asiaList(map);
	}

	@Override
	public long europeCount(String user_id) throws Exception {
		return recommendMapper.europeCount(user_id);
	}

	@Override
	public List<SongVO> europeList(Map<String, Object> map) throws Exception {
		return recommendMapper.europeList(map);
	}

	@Override
	public long group_girlCount(String user_id) throws Exception {
		return recommendMapper.group_girlCount(user_id);
	}

	@Override
	public List<SongVO> group_girlList(Map<String, Object> map) throws Exception {
		return recommendMapper.group_girlList(map);
	}

	@Override
	public long group_boyCount(String user_id) throws Exception {
		return recommendMapper.group_boyCount(user_id);
	}

	@Override
	public List<SongVO> group_boyList(Map<String, Object> map) throws Exception {
		return recommendMapper.group_boyList(map);
	}

	@Override
	public long group_hybridCount(String user_id) throws Exception {
		return recommendMapper.group_hybridCount(user_id);
	}

	@Override
	public List<SongVO> group_hybridList(Map<String, Object> map) throws Exception {
		return recommendMapper.group_hybridList(map);
	}

	@Override
	public long solo_girlCount(String user_id) throws Exception {
		return recommendMapper.solo_girlCount(user_id);
	}

	@Override
	public List<SongVO> solo_girlList(Map<String, Object> map) throws Exception {
		return recommendMapper.solo_girlList(map);
	}

	@Override
	public long solo_boyCount(String user_id) throws Exception {
		return recommendMapper.solo_boyCount(user_id);
	}

	@Override
	public List<SongVO> solo_boyList(Map<String, Object> map) throws Exception {
		return recommendMapper.solo_boyList(map);
	}

	@Override
	public long keyword_lieCount(String user_id) throws Exception {
		return recommendMapper.keyword_lieCount(user_id);
	}

	@Override
	public List<SongVO> keyword_lieList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_lieList(map);
	}

	@Override
	public long keyword_RemakeCount(String user_id) throws Exception {
		return recommendMapper.keyword_RemakeCount(user_id);
	}

	@Override
	public List<SongVO> keyword_RemakeList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_RemakeList(map);
	}

	@Override
	public long keyword_trustCount(String user_id) throws Exception {
		return recommendMapper.keyword_trustCount(user_id);
	}

	@Override
	public List<SongVO> keyword_trustList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_trustList(map);
	}

	@Override
	public long keyword_loveCount(String user_id) throws Exception {
		return recommendMapper.keyword_loveCount(user_id);
	}

	@Override
	public List<SongVO> keyword_loveList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_loveList(map);
	}

	@Override
	public long keyword_RemixCount(String user_id) throws Exception {
		return recommendMapper.keyword_RemixCount(user_id);
	}

	@Override
	public List<SongVO> keyword_RemixList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_RemixList(map);
	}

	@Override
	public long keyword_birthdayCount(String user_id) throws Exception {
		return recommendMapper.keyword_birthdayCount(user_id);
	}

	@Override
	public List<SongVO> keyword_birthdayList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_birthdayList(map);
	}

	@Override
	public long keyword_wishCount(String user_id) throws Exception {
		return recommendMapper.keyword_wishCount(user_id);
	}

	@Override
	public List<SongVO> keyword_wishList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_wishList(map);
	}

	@Override
	public long keyword_timeCount(String user_id) throws Exception {
		return recommendMapper.keyword_timeCount(user_id);
	}

	@Override
	public List<SongVO> keyword_timeList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_timeList(map);
	}

	@Override
	public long keyword_partingCount(String user_id) throws Exception {
		return recommendMapper.keyword_partingCount(user_id);
	}

	@Override
	public List<SongVO> keyword_partingList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_partingList(map);
	}

	@Override
	public long keyword_storyCount(String user_id) throws Exception {
		return recommendMapper.keyword_storyCount(user_id);
	}

	@Override
	public List<SongVO> keyword_storyList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_storyList(map);
	}

	@Override
	public long keyword_friendCount(String user_id) throws Exception {
		return recommendMapper.keyword_friendCount(user_id);
	}

	@Override
	public List<SongVO> keyword_friendList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_friendList(map);
	}

	@Override
	public long keyword_talkCount(String user_id) throws Exception {
		return recommendMapper.keyword_talkCount(user_id);
	}

	@Override
	public List<SongVO> keyword_talkList(Map<String, Object> map) throws Exception {
		return recommendMapper.keyword_talkList(map);
	}

	@Override
	public long favorite_singerCount(String user_id) throws Exception {
		return recommendMapper.favorite_singerCount(user_id);
	}

	@Override
	public List<SongVO> favorite_singerList(Map<String, Object> map) throws Exception {
		return recommendMapper.favorite_singerList(map);
	}

	@Override
	public long high_starpointCount(String user_id) throws Exception {
		return recommendMapper.high_starpointCount(user_id);
	}

	@Override
	public List<SongVO> high_starpointList(Map<String, Object> map) throws Exception {
		return recommendMapper.high_starpointList(map);
	}

	@Override
	public long favorite_issue_dateCount(String user_id) throws Exception {
		return recommendMapper.favorite_issue_dateCount(user_id);
	}

	@Override
	public List<SongVO> favorite_issue_dateList(Map<String, Object> map) throws Exception {
		return recommendMapper.favorite_issue_dateList(map);
	}

	@Override
	public long favorite_play_timeCount(String user_id) throws Exception {
		return recommendMapper.favorite_play_timeCount(user_id);
	}

	@Override
	public List<SongVO> favorite_play_timeList(Map<String, Object> map) throws Exception {
		return recommendMapper.favorite_play_timeList(map);
	}

	@Override
	public long getTotalCommentCount(RecommendVO recommendVO) throws Exception {
		return recommendMapper.getTotalCommentCount(recommendVO);
	}

	@Override
	public long getRecommendCount(long user_no) throws Exception {
		return recommendMapper.getRecommendCount(user_no);
	}

	@Override
	public long getUserRecommendTotalCount(long user_no) throws Exception {
		return recommendMapper.getUserRecommendTotalCount(user_no);
	}




}
