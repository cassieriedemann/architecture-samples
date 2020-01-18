package com.example.android.architecture.blueprints.todoapp.addedittask

data class AddEditTaskViewState(
        val taskId: Int?,
        val title: String,
        val description: String,
        val loading: Boolean,
        val snackbarMessage: String?,
        val taskUpdated: Boolean?
)