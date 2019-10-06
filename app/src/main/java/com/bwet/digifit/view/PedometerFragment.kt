package com.bwet.digifit.view


import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bwet.digifit.R
import com.bwet.digifit.utils.DEBUG_TAG
import com.bwet.digifit.utils.RuntimePermissionUtil
import com.bwet.digifit.viewModel.StepViewModel
import kotlinx.android.synthetic.main.fragment_pedometer.*
import kotlinx.android.synthetic.main.fragment_pedometer.view.*
import kotlinx.coroutines.launch


class PedometerFragment : BaseFragment() {

    private lateinit var stepViewModel: StepViewModel

    companion object {
        @JvmStatic
        fun newInstance() = PedometerFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pedometer, container, false)

        activity?.let { RuntimePermissionUtil.getInstance(it).requestActivityRecognition() }

        stepViewModel = ViewModelProviders.of(this).get(StepViewModel::class.java)
        stepViewModel.getTotalStepsLive().observe(this, Observer {
            view.step_count_txt.text = it.toString()
            animateProgressBar(it)
        })

        view.test_btn.setOnClickListener {
            val startTime = System.currentTimeMillis() - 7200 * 1000
            val endTime = System.currentTimeMillis()
            launch {
                stepViewModel.getStepCountByInterval(startTime, endTime, 3600, "%H")
                    .forEach { Log.d(DEBUG_TAG, "count ${it.count} intervalFormat ${it.intervalFormat}") }
            }
        }

        return view
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RuntimePermissionUtil.PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //permission granted
                } else {
                    activity?.let {
                        val runtimePermissionUtil = RuntimePermissionUtil.getInstance(it)
                        runtimePermissionUtil.showDialogAndAsk(
                            getString(R.string.activity_recognition),
                            DialogInterface.OnClickListener{_, _ -> runtimePermissionUtil.requestActivityRecognition()}
                        )
                    }
                }
            }
        }
    }

    // Animate progressbar from last progress value up to the newValue passed to function
    private fun animateProgressBar(newValue: Int) {
        val animator = ObjectAnimator.ofInt(step_progressBar, "progress", step_progressBar.progress , newValue)
        animator.duration = 5000
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        step_progressBar.clearAnimation()
    }
}
