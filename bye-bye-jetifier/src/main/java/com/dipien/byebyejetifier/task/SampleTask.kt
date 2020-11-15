package com.dipien.byebyejetifier.task

import com.dipien.byebyejetifier.common.AbstractTask

open class SampleTask : AbstractTask() {

    companion object {
        const val TASK_NAME = "sample"
    }

    init {
        description = "Sample task"
    }

    override fun onExecute() {
        // .
    }
}
