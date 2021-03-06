package com.ebookfrenzy.citationneeded

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CitationRepository(application : Application) {
    private var citationDao : CitationDao? = null //the DAO, contains all our queries
    private var searchByTagResult : MutableLiveData<List<Citation>> = MutableLiveData() //results of searching by tag
    private var searchByAuthorResult : MutableLiveData<List<Citation>> = MutableLiveData()
    private var searchByBookResult : MutableLiveData<List<Citation>> = MutableLiveData()
    init {
        //getting room database and DAO
        val db : CitationRoomDatabase? = CitationRoomDatabase.getDatabase(application)
        citationDao = db?.citationDao()
    }

    fun insertCitation(newCitation : Citation, tags : List<Tag>) {
        //insert one record into CITATION table and up to 3 records into TAG table
        //this runs on an IO thread
        CoroutineScope(IO).launch {
            val citationID = insertCitationAsync(newCitation)
            for (tag in tags) {
                if (citationID != null) {
                    tag.citationID = citationID
                }
                insertTagAsync(tag)
            }
        }
    }

    private suspend fun insertCitationAsync(newCitation : Citation) : Long? {
        return citationDao?.insertCitation(newCitation)
    }

    private suspend fun insertTagAsync(tag : Tag) {
        citationDao?.insertTag(tag)
    }

    fun searchByTag(tag : String) {
        CoroutineScope(IO).launch {
            //query CITATION table by tag and assign to MutableLiveData
            //assignment is done on main thread and not background thread
            searchByTagResult.postValue(searchByTagAsync(tag))
        }
    }

    private suspend fun searchByTagAsync(tag : String) : List<Citation>? {
        return citationDao?.searchByTag(tag)
    }

    fun getSearchByTagResult() : MutableLiveData<List<Citation>> {
        //return MutableLiveData
        return searchByTagResult
    }


    fun getSearchByAuthorResult() : MutableLiveData<List<Citation>> {
        //return MutableLiveData
        return searchByAuthorResult
    }


    private suspend  fun  searchByAuthorAsync(firstName: String, lastName:String):List<Citation>?{
        return citationDao?.filterByAuthor(firstName, lastName)
    }
    fun searchByAuthor(firstName: String, lastName:String){
        CoroutineScope(IO).launch {
            //query CITATION table by tag and assign to MutableLiveData
            //assignment is done on main thread and not background thread
            val list = searchByAuthorAsync(firstName, lastName);
            searchByAuthorResult.postValue(list)
        }
    }

    fun getSearchByBookResult() : MutableLiveData<List<Citation>> {
        //returns mutable data
        return searchByBookResult
    }

    private suspend fun  searchByBookTitleAsync(bookTitle : String):List<Citation>?{
        return citationDao?.filterByBook(bookTitle)
    }

    fun searchByBookTitle(bookTitle : String){
        CoroutineScope(IO).launch {
            //query CITATION table by tag and assign to MutableLiveData
            //assignment is done on main thread and not background thread
            val list = searchByBookTitleAsync(bookTitle);
            searchByBookResult.postValue(list)
        }
    }

    fun deleteCitation(citationID : Long) {
        //delete record in CITATION table and up to 3 records in TAG table
        CoroutineScope(IO).launch {
            deleteCitationAsync(citationID)
        }
    }

    private fun deleteCitationAsync(citationID : Long) {
        citationDao?.deleteCitation(citationID)
        citationDao?.deleteTags(citationID)
    }
}