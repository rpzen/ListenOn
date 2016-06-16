package net.musicrecommend.www.recommend;

import java.util.List;
import java.util.Map;

import net.musicrecommend.www.vo.AlbumVO;
import net.musicrecommend.www.vo.ArtistVO;
import net.musicrecommend.www.vo.DetailChartVO;
import net.musicrecommend.www.vo.KeywordVO;
import net.musicrecommend.www.vo.RecommendUserVO;
import net.musicrecommend.www.vo.SongVO;

public interface RecommendMapper {

	int insertRecommend(RecommendVO recommendVO) throws Exception;

	int checkRecommendExist(RecommendVO recommendVO) throws Exception;

	int updateRecommend(RecommendVO recommendVO) throws Exception;

	long getTotalCount() throws Exception;

	List<RecommendVO> getUserRecommendList(long user_no) throws Exception;

	int insertComment(RecommendVO recommendVO) throws Exception;

	int checkCommentExist(RecommendVO recommendVO) throws Exception;

	List<SongVO> getNullalbmSongs() throws Exception;

	void insertNullalbum(SongVO songVO) throws Exception;

	int updateUserComment(RecommendVO recommendVO) throws Exception;

	RecommendVO getUseRecommendInfo(long user_no) throws Exception;

	List<RecommendUserVO> getCommentList(Map<String, Object> mapData) throws Exception;

	List<DetailChartVO> getStarPointSubtotal(RecommendVO recommendVO) throws Exception;

	List<DetailChartVO> getStarPointAvg(RecommendVO recommendVO) throws Exception;

	List<KeywordVO> getSearchKeywordList() throws Exception;

	List<SongVO> getHighRatedSongs() throws Exception;

	List<AlbumVO> getHighRatedAlbums() throws Exception;

	List<ArtistVO> getHighRatedArtists() throws Exception;

	List<ArtistVO> getHighRatedArtistsDefault(String genre) throws Exception;

	List<ArtistVO> getHighRatedArtistsUser(Map param) throws Exception;

	List<SongVO> getXCenturySongs(String year) throws Exception;

	List<Map<String, Object>> getRecommendRank() throws Exception;

	List<SongVO> getAdultSongList() throws Exception;
	
	List<SongVO> getSurvivalSongList() throws Exception;
	
	

	// 권우\\
	long koreaCount(String user_id) throws Exception;

	List<SongVO> koreaList(Map<String, Object> map) throws Exception;

	long americaCount(String user_id) throws Exception;

	List<SongVO> americaList(Map<String, Object> map) throws Exception;

	long asiaCount(String user_id) throws Exception;

	List<SongVO> asiaList(Map<String, Object> map) throws Exception;

	long europeCount(String user_id) throws Exception;

	List<SongVO> europeList(Map<String, Object> map) throws Exception;

	long group_girlCount(String user_id) throws Exception;

	List<SongVO> group_girlList(Map<String, Object> map) throws Exception;

	long group_boyCount(String user_id) throws Exception;

	List<SongVO> group_boyList(Map<String, Object> map) throws Exception;

	long group_hybridCount(String user_id) throws Exception;

	List<SongVO> group_hybridList(Map<String, Object> map) throws Exception;

	long solo_girlCount(String user_id) throws Exception;

	List<SongVO> solo_girlList(Map<String, Object> map) throws Exception;

	long solo_boyCount(String user_id) throws Exception;

	List<SongVO> solo_boyList(Map<String, Object> map) throws Exception;

	long keyword_lieCount(String user_id) throws Exception;

	List<SongVO> keyword_lieList(Map<String, Object> map) throws Exception;

	long keyword_RemakeCount(String user_id) throws Exception;

	List<SongVO> keyword_RemakeList(Map<String, Object> map) throws Exception;

	long keyword_trustCount(String user_id) throws Exception;

	List<SongVO> keyword_trustList(Map<String, Object> map) throws Exception;

	long keyword_loveCount(String user_id) throws Exception;

	List<SongVO> keyword_loveList(Map<String, Object> map) throws Exception;

	long keyword_RemixCount(String user_id) throws Exception;

	List<SongVO> keyword_RemixList(Map<String, Object> map) throws Exception;

	long keyword_birthdayCount(String user_id) throws Exception;

	List<SongVO> keyword_birthdayList(Map<String, Object> map) throws Exception;

	long keyword_wishCount(String user_id) throws Exception;

	List<SongVO> keyword_wishList(Map<String, Object> map) throws Exception;

	long keyword_timeCount(String user_id) throws Exception;

	List<SongVO> keyword_timeList(Map<String, Object> map) throws Exception;

	long keyword_partingCount(String user_id) throws Exception;

	List<SongVO> keyword_partingList(Map<String, Object> map) throws Exception;

	long keyword_storyCount(String user_id) throws Exception;

	List<SongVO> keyword_storyList(Map<String, Object> map) throws Exception;

	long keyword_friendCount(String user_id) throws Exception;

	List<SongVO> keyword_friendList(Map<String, Object> map) throws Exception;

	long keyword_talkCount(String user_id) throws Exception;

	List<SongVO> keyword_talkList(Map<String, Object> map) throws Exception;

	long favorite_singerCount(String user_id) throws Exception;

	List<SongVO> favorite_singerList(Map<String, Object> map) throws Exception;

	long high_starpointCount(String user_id) throws Exception;

	List<SongVO> high_starpointList(Map<String, Object> map) throws Exception;

	long favorite_issue_dateCount(String user_id) throws Exception;

	List<SongVO> favorite_issue_dateList(Map<String, Object> map) throws Exception;

	long favorite_play_timeCount(String user_id) throws Exception;

	List<SongVO> favorite_play_timeList(Map<String, Object> map) throws Exception;

	long getTotalCommentCount(RecommendVO recommendVO) throws Exception;

	long getRecommendCount(long user_no) throws Exception;

	long getUserRecommendTotalCount(long user_no) throws Exception;

}
