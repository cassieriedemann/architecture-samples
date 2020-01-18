package com.example.android.architecture.blueprints.todoapp.savedataexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
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

abstract class BaseSavedDataFragment<V: ViewModel, B: ViewDataBinding> : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    protected lateinit var binding: B

    @get:LayoutRes
    protected abstract val layoutId: Int

    abstract val vm: ViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = this
        binding.setVariable(BR.viewmodel, vm)
        return binding.root
    }
}

class SavedDataFragment : BaseSavedDataFragment<SavedDataVm, SavedataFragBinding>() {
    override val vm: SavedDataVm by viewModels { viewModelFactory }
    override val layoutId = R.layout.savedata_frag
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