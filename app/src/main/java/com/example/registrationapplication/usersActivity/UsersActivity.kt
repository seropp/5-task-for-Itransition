package com.example.registrationapplication.usersActivity

import com.example.registrationapplication.R


import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.registrationapplication.Model
import com.example.registrationapplication.adapter.Adapter
import com.example.registrationapplication.mainAct1.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User

class UsersActivity : AppCompatActivity() {



    lateinit var adapter: Adapter
    lateinit var idForDeleted: String
    lateinit var recyclerView: RecyclerView


    private lateinit var usersList: ArrayList<Model>
    private lateinit var root: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        recyclerView = findViewById(R.id.userList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = Adapter()
        recyclerView.adapter = adapter
        usersList = arrayListOf()
        root = FirebaseDatabase.getInstance().getReference("Users")

        getUserData()

        adapter?.setOnClickBlockItemOrUnBlock {
            updateUserStatus(it.id!!)
            getUserData()
        }

        adapter?.setOnClickDeleteItem {
            deleteUser(it.id!!)
            getUserData()
        }

        setItemTouchHelper()
    }

    private fun getUserData() {
        root.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    usersList.removeAll { true }
                    for (dataSnapshot in snapshot.children) {
                        val model = dataSnapshot.getValue(Model::class.java)

                        usersList.add(model!!)
                    }
                    adapter?.addItems(usersList)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun updateUserStatus(id: String) {
        root.child(id).get().addOnSuccessListener {
            val status = it.child("status").value.toString()

            val newStatus = when (status) {
                "not blocked" -> "blocked"
                else -> "not blocked"
            }

            val updateUser = mapOf(
                "status" to newStatus,
            )

            root.child(id).updateChildren(updateUser).addOnSuccessListener {
                if (newStatus == "blocked") {
                    Toast.makeText(this, "User has been blocked", Toast.LENGTH_SHORT).show()
                    if (id == MainActivity.currentId) {
                        returnToAuthWin()
                    }
                } else {
                    Toast.makeText(this, "User has been unblocked", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }
        }
    }

    private fun deleteUser(id: String) {

        root.child(id).removeValue().addOnSuccessListener {
            Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show()
            if (id == MainActivity.currentId) {
                returnToAuthWin()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnToAuthWin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setItemTouchHelper() {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            private val limitScrollX = dipToPix(100f, this@UsersActivity)
            var currentScrollX = 0
            var currentScrollXWhenInActive = 0
            var initXWhenInActive = 0f
            var firstInActive = false

            override fun getMovementFlags(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = 0
                val swipedFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, swipedFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return Integer.MAX_VALUE.toFloat()
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return Integer.MAX_VALUE.toFloat()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (dX == 0f) {
                        currentScrollX = viewHolder.itemView.scrollX
                        firstInActive = true
                    }
                    if (isCurrentlyActive) {
                        var scrollOffset = currentScrollX + (-dX).toInt()
                        if (scrollOffset > limitScrollX) {
                            scrollOffset = limitScrollX
                        } else if (scrollOffset < 0) {
                            scrollOffset = 0
                        }
                        viewHolder.itemView.scrollTo(scrollOffset, 0)
                    } else {
                        if (firstInActive) {
                            firstInActive = false
                            currentScrollXWhenInActive = viewHolder.itemView.scrollX
                            initXWhenInActive = dX
                        }
                        if (viewHolder.itemView.scrollX < limitScrollX) {
                            viewHolder.itemView.scrollTo(
                                (currentScrollXWhenInActive * dX / initXWhenInActive).toInt(),
                                0
                            )
                        }
                    }
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                if (viewHolder.itemView.scrollX > limitScrollX) {
                    viewHolder.itemView.scrollTo(limitScrollX, 0)
                } else if (viewHolder.itemView.scrollX < 0) {
                    viewHolder.itemView.scrollTo(0, 0)
                }
            }
        }).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    private fun dipToPix(diValue: Float, context: Context): Int {
        return (diValue * context.resources.displayMetrics.density).toInt()
    }

}