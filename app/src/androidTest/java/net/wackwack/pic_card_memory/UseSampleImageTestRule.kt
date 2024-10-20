package net.wackwack.pic_card_memory

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StreamDownloadTask
import com.google.firebase.storage.ktx.storage
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class UseSampleImageTestRule: TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        class DownloadStatement: Statement() {
            override fun evaluate() {
                Log.d(javaClass.simpleName, "get images list")
                val storage = Firebase.storage
                val resolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
                val listResult = storage.reference.root.listAll()
                val allTask = arrayListOf<StreamDownloadTask>()
                listResult.addOnSuccessListener { list ->
                    list.items.forEach { item ->
                        Log.d(javaClass.simpleName, item.name)
                        val newValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            put(MediaStore.MediaColumns.DISPLAY_NAME, item.name)
                        }
                        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), newValues)
                        val outputStream = resolver.openOutputStream(uri!!)
                        val task = item.getStream { _, stream ->
                            val buffer = ByteArray(1024)
                            while (stream.read(buffer) != -1) {
                                outputStream?.write(buffer)
                            }
                        }
                        task.addOnCompleteListener {
                            Log.d(javaClass.simpleName, "save: $uri")
                            outputStream?.close()
                        }.addOnFailureListener {
                            Log.e(javaClass.simpleName, it.message!!)
                            outputStream?.close()
                        }

                        allTask.add(task)
                    }
                }.addOnFailureListener { e ->
                    Log.e(javaClass.simpleName, e.message!!)
                }

                while (!listResult.isComplete) {
                    Thread.sleep(1000)
                    Log.d(javaClass.simpleName, "waiting get image list...")
                }

                while (!allTask.stream().allMatch {
                        it.isComplete
                    }
                ) {
                    Thread.sleep(1000)
                    Log.d(javaClass.simpleName, "waiting save image...")
                }
                base.evaluate()
            }
        }
        return DownloadStatement()
    }
}