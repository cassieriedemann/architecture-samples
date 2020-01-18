package com.example.android.architecture.blueprints.todoapp.savedataexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.databinding.SavedataFragBinding
import com.example.android.architecture.blueprints.todoapp.di.ViewModelAssistedFactory
import com.example.android.architecture.blueprints.todoapp.di.ViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseSavedDataFragment<V: ViewModel> : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    abstract val vm: ViewModel
}

class SavedDataFragment : BaseSavedDataFragment<SavedDataVm>() {
    private lateinit var viewDataBinding: SavedataFragBinding

    override val vm: SavedDataVm by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.savedata_frag, container, false)
        viewDataBinding = SavedataFragBinding.bind(root).apply {
            this.viewmodel = vm
        }

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }
}

const val TITLE_KEY = "title"
const val DESCRIPTION_KEY = "description"

class SavedDataVm @AssistedInject constructor (
        @Assisted private val handle: SavedStateHandle,
        private val tasksRepository: TasksRepository
) : ViewModel() {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    init {
        title.postValue(handle.get(TITLE_KEY) ?: "")
        description.postValue(handle[DESCRIPTION_KEY] ?: "")
    }

    fun saveData() {
        handle[TITLE_KEY] = title.value
        handle[DESCRIPTION_KEY] = description.value
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<SavedDataVm>
}