package net.wackwack.pic_card_memory.settings.stub

import android.content.SharedPreferences

class StubSharedPreferencesImpl: SharedPreferences {
    private val editor = StubEditor()
    fun removeAll() {
        editor.removeAll()
    }
    override fun contains(key: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return editor.intStore[key] ?: defValue
    }

    override fun getAll(): MutableMap<String, *> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun edit(): SharedPreferences.Editor {
        return editor
    }

    override fun getLong(key: String?, defValue: Long): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return editor.floatStore[key] ?: defValue
    }

    override fun getStringSet(
        key: String?,
        defValues: MutableSet<String>?
    ): MutableSet<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getString(key: String?, defValue: String?): String? {
        return editor.stringStore[key] ?: defValue
    }
}

class StubEditor: SharedPreferences.Editor {
    val stringStore:HashMap<String,String> = hashMapOf()
    val intStore:HashMap<String,Int> = hashMapOf()
    val floatStore:HashMap<String,Float> = hashMapOf()
    fun removeAll() {
        stringStore.clear()
        intStore.clear()
        floatStore.clear()
    }
    override fun clear(): SharedPreferences.Editor {
        stringStore.clear()
        return this
    }

    override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
        key?.let{ intStore[it]=value}
        return this
    }

    override fun remove(key: String?): SharedPreferences.Editor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun commit(): Boolean {
        return true
    }

    override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
        key?.let{floatStore[it]=value}
        return this
    }

    override fun apply() {
        println("Called apply()")
    }

    override fun putString(key: String?, value: String?): SharedPreferences.Editor {
        key?.let{value?.let{value->stringStore[it] = value}}
        return this
    }
}