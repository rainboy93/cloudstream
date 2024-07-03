package com.lagradost.cloudstream3.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Window
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.lagradost.cloudstream3.databinding.UpdateDialogBinding

class UpdateDialog : DialogFragment() {

    private lateinit var viewBinding: UpdateDialogBinding

    private val message by lazy { arguments?.getString(KEY_MESSAGE) ?: "" }
    private val groupLink by lazy { arguments?.getString(KEY_LINK) ?: "" }

    private val onBackPressedListener = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setOnKeyListener { _, keyCode, _ ->
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                    return@setOnKeyListener true
                } else {
                    return@setOnKeyListener false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = UpdateDialogBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            onBackPressedListener
        )
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        viewBinding.message.text = "$message $groupLink"
        viewBinding.buttonUpdate.setOnClickListener {
            try {
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(groupLink)
                    startActivity(this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            val qrgEncoder = QRGEncoder(groupLink, null, QRGContents.Type.TEXT, 120).apply {
                colorBlack = Color.WHITE
                colorWhite = Color.BLACK
            }
            viewBinding.qrCode.setImageBitmap(qrgEncoder.bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_MESSAGE = "Message"
        private const val KEY_LINK = "Link"

        fun paramsBuilder(message: String, groupLink: String): Bundle {
            return bundleOf(KEY_MESSAGE to message, KEY_LINK to groupLink)
        }
    }
}