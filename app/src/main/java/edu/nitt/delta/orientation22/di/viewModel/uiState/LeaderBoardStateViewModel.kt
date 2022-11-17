package edu.nitt.delta.orientation22.di.viewModel.uiState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.nitt.delta.orientation22.di.viewModel.BaseViewModel
import edu.nitt.delta.orientation22.di.viewModel.actions.LeaderBoardAction
import edu.nitt.delta.orientation22.di.viewModel.repository.LeaderBoardRepository
import edu.nitt.delta.orientation22.models.Result
import edu.nitt.delta.orientation22.models.leaderboard.LeaderboardData
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderBoardStateViewModel @Inject constructor(
    private val leaderBoardRepository: LeaderBoardRepository
):BaseViewModel<LeaderBoardAction>() {
    private val leaderBoardSample: List<LeaderboardData> = listOf()
    var leaderBoardData by mutableStateOf<List<LeaderboardData>>(leaderBoardSample)
    override fun doAction(action: LeaderBoardAction): Any =when(action){
        is LeaderBoardAction.GetLeaderBoard -> getLeaderBoard()
    }

    private fun getLeaderBoard()=launch {
        val token = ""
        when(val res = leaderBoardRepository.getLeaderBoard(token)){
            is Result.Value-> leaderBoardData = res.value
            is Result.Error -> mutableError.value = res.exception.message
        }
    }
}
