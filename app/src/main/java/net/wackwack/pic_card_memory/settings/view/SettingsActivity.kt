package net.wackwack.pic_card_memory.settings.view

import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import net.wackwack.pic_card_memory.databinding.ActivitySettingsBinding
import net.wackwack.pic_card_memory.settings.model.NumOfCard
import net.wackwack.pic_card_memory.settings.viewmodel.CommandSettings
import net.wackwack.pic_card_memory.settings.viewmodel.SettingsViewModel
import net.wackwack.pic_card_memory.settings.model.ImagePathType

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var dataBinding: ActivitySettingsBinding

    private val launcher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result->
                Log.d(javaClass.simpleName, result.data?.data.toString())
                if(result.resultCode == Activity.RESULT_OK) {
                    //Configに登録
                    result.data?.data?.also { uri ->
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        viewModel.updateImagePathType(ImagePathType.SPECIFIED, uri.toString())
                    }?: run {
                        viewModel.updateImagePathType(ImagePathType.EXTERNAL, "")
                    }
                } else {
                    // 今のConfigが無ければSDカードに設定
                    viewModel.updateImagePathType(ImagePathType.EXTERNAL, "")
                }
            }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)


        dataBinding.toggleNumOfCard12.setOnClickListener {
            viewModel.updateNumOfCard(NumOfCard.TWELVE)
        }
        dataBinding.toggleNumOfCard20.setOnClickListener {
            viewModel.updateNumOfCard(NumOfCard.TWENTY)
        }
        dataBinding.toggleNumOfCard30.setOnClickListener {
            viewModel.updateNumOfCard(NumOfCard.THIRTY)
        }
        dataBinding.radioSDCard.setOnClickListener {
            viewModel.updateImagePathType(ImagePathType.EXTERNAL, "")
        }
        dataBinding.radioSpecifyDirectory.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            launcher.launch(intent)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.message.collect { command->
                Log.d(javaClass.simpleName, "Receive Message")
                when(command) {
                    is CommandSettings.UpdateNumOfCard -> {
                        dataBinding.toggleNumOfCard12.isChecked = command.numOfCard == NumOfCard.TWELVE
                        dataBinding.toggleNumOfCard12.isClickable = command.numOfCard != NumOfCard.TWELVE
                        dataBinding.toggleNumOfCard20.isChecked = command.numOfCard == NumOfCard.TWENTY
                        dataBinding.toggleNumOfCard20.isClickable = command.numOfCard != NumOfCard.TWENTY
                        dataBinding.toggleNumOfCard30.isChecked = command.numOfCard == NumOfCard.THIRTY
                        dataBinding.toggleNumOfCard30.isClickable = command.numOfCard != NumOfCard.THIRTY
                    }
                    is CommandSettings.UpdateImagePathType -> {
                        when(command.pathType) {
                            ImagePathType.EXTERNAL -> {
                                dataBinding.radioSDCard.isChecked = true
                                dataBinding.textSpecifiedDirectoryPath.text = ""
                                dataBinding.textSpecifiedDirectoryPath.visibility = View.INVISIBLE
                            }
                            ImagePathType.SPECIFIED -> {
                                dataBinding.radioSpecifyDirectory.isChecked = true
                                dataBinding.textSpecifiedDirectoryPath.text = command.path
                                dataBinding.textSpecifiedDirectoryPath.visibility = View.VISIBLE
                            }
                        }
                    }
                    else -> {
                        Log.w(javaClass.simpleName, "Unexpected message: $command")
                    }
                }
            }
        }
        viewModel.init()
    }
}