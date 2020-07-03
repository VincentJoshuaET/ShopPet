package com.vt.shoppet.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.ChangeEventListener
import com.firebase.ui.firestore.ClassSnapshotParser
import com.firebase.ui.firestore.FirestoreArray
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vt.shoppet.livedata.DocumentLiveData
import com.vt.shoppet.livedata.QueryLiveData
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo

class DataViewModel @ViewModelInject constructor(
    private val auth: AuthRepo,
    private val firestore: FirestoreRepo,
    @Assisted private val handle: SavedStateHandle
) : ViewModel(), DefaultLifecycleObserver {

    private val currentPet = MutableLiveData<Pet>()
    private val currentUser = handle.getLiveData<User>("currentUser")
    private val user = MutableLiveData<User>()
    private val chat = MutableLiveData<Chat>()
    private val pets = MutableLiveData<List<Pet>>()

    private var firestorePets = MutableLiveData<FirestoreArray<Pet>>()
    private var firestoreStarredPets = MutableLiveData<FirestoreArray<Pet>>()
    private var firestoreOwnPets = MutableLiveData<FirestoreArray<Pet>>()
    private var firestoreChats = MutableLiveData<FirestoreArray<Chat>>()

    fun getPets(): LiveData<List<Pet>> = pets

    fun getCurrentUser(): LiveData<User> = currentUser

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

    fun initFirebaseData() {
        DocumentLiveData(firestore.getUser(auth.uid())).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> currentUser.value = result.data.toObject()
            }
        }

        QueryLiveData(firestore.getPets()).observe(ProcessLifecycleOwner.get()) { result ->
            when (result) {
                is Result.Success -> pets.value = result.data.toObjects()
            }
        }

        firestorePets.value =
            FirestoreArray(firestore.getPets(), ClassSnapshotParser(Pet::class.java))
        firestoreStarredPets.value =
            FirestoreArray(firestore.getStarredPets(), ClassSnapshotParser(Pet::class.java))
        firestoreOwnPets.value =
            FirestoreArray(firestore.getOwnPets(), ClassSnapshotParser(Pet::class.java))
        firestoreChats.value =
            FirestoreArray(firestore.getChats(), ClassSnapshotParser(Chat::class.java))
        handle.set("initFirebaseData", true)
        onStart(ProcessLifecycleOwner.get())
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        handle.get<Boolean>("initFirebaseData")?.let { boolean ->
            if (boolean) initFirebaseData()
        }
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