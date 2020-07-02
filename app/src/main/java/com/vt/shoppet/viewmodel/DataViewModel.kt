package com.vt.shoppet.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.ChangeEventListener
import com.firebase.ui.firestore.ClassSnapshotParser
import com.firebase.ui.firestore.FirestoreArray
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vt.shoppet.livedata.DocumentLiveData
import com.vt.shoppet.livedata.QueryLiveData
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import com.vt.shoppet.repo.FirestoreRepo

class DataViewModel @ViewModelInject constructor(
    private val repo: FirestoreRepo,
    @Assisted private val handle: SavedStateHandle
) : ViewModel(), DefaultLifecycleObserver {

    private val currentPet = MutableLiveData<Pet>()
    private val currentUser = MutableLiveData<User>()
    private val user = MutableLiveData<User>()
    private val chat = MutableLiveData<Chat>()
    private val pets = MutableLiveData<List<Pet>>()

    private var firestorePets =
        MutableLiveData(FirestoreArray(repo.getPets(), ClassSnapshotParser(Pet::class.java)))
    private var firestoreStarredPets =
        MutableLiveData(FirestoreArray(repo.getStarredPets(), ClassSnapshotParser(Pet::class.java)))
    private var firestoreOwnPets =
        MutableLiveData(FirestoreArray(repo.getOwnPets(), ClassSnapshotParser(Pet::class.java)))
    private var firestoreChats =
        MutableLiveData(FirestoreArray(repo.getChats(), ClassSnapshotParser(Chat::class.java)))

    fun getPets(): LiveData<List<Pet>> = pets

    fun setPetLiveData() =
        QueryLiveData(repo.getPets()).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> pets.value = result.data.toObjects()
            }
        }

    fun getCurrentUser(): LiveData<User> = currentUser

    fun setUserLiveData(uid: String) =
        DocumentLiveData(repo.getUser(uid)).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> currentUser.value = result.data.toObject()
            }
        }

    fun getUser(): LiveData<User> = user

    fun setUser(user: User) {
        this.user.value = user
    }

    fun clearUser() {
        user.value = null
    }

    fun getCurrentPet(): LiveData<Pet> = currentPet

    fun setCurrentPet(pet: Pet) {
        currentPet.value = pet
    }

    fun getChat(): LiveData<Chat> = chat

    fun setChat(chat: Chat) {
        this.chat.value = chat
    }

    fun getFirestorePets() = firestorePets
    fun getFirestoreStarredPets() = firestoreStarredPets
    fun getFirestoreOwnPets() = firestoreOwnPets
    fun getFirestoreChats() = firestoreChats

    fun setFirestoreArrays() {
        firestorePets.value = FirestoreArray(repo.getPets(), ClassSnapshotParser(Pet::class.java))
        firestoreStarredPets.value =
            FirestoreArray(repo.getStarredPets(), ClassSnapshotParser(Pet::class.java))
        firestoreOwnPets.value =
            FirestoreArray(repo.getOwnPets(), ClassSnapshotParser(Pet::class.java))
        firestoreChats.value =
            FirestoreArray(repo.getChats(), ClassSnapshotParser(Chat::class.java))
        handle.set("arrays", true)
        onStart(ProcessLifecycleOwner.get())
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        firestorePets.value?.addChangeEventListener(KeepAliveListener)
        firestoreStarredPets.value?.addChangeEventListener(KeepAliveListener)
        firestoreOwnPets.value?.addChangeEventListener(KeepAliveListener)
        firestoreChats.value?.addChangeEventListener(KeepAliveListener)
    }

    override fun onStop(owner: LifecycleOwner) {
        firestorePets.value?.removeChangeEventListener(KeepAliveListener)
        firestoreStarredPets.value?.removeChangeEventListener(KeepAliveListener)
        firestoreOwnPets.value?.removeChangeEventListener(KeepAliveListener)
        firestoreChats.value?.removeChangeEventListener(KeepAliveListener)
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        onStop(ProcessLifecycleOwner.get())
    }

    private object KeepAliveListener : ChangeEventListener {
        override fun onDataChanged() = Unit
        override fun onChildChanged(
            type: ChangeEventType,
            snapshot: DocumentSnapshot,
            newIndex: Int,
            oldIndex: Int
        ) = Unit

        override fun onError(e: FirebaseFirestoreException) = Unit
    }

}