package com.example.studentregister

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentregister.db.Student
import com.example.studentregister.db.StudentDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnClear: Button
    private lateinit var rvStudent: RecyclerView

    private lateinit var viewModel: StudentViewModel

    private lateinit var adapter: StudentRecyclerViewAdapter

    private lateinit var selectedStudent: Student
    private var lisItemClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        btnSave = findViewById(R.id.btnSave)
        btnClear = findViewById(R.id.btnClear)

        rvStudent = findViewById(R.id.rvStudent)

        val dao = StudentDatabase.getInstance(application).studentDao()
        val factory = StudentViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory).get(StudentViewModel::class.java)

        btnSave.setOnClickListener {
            if (lisItemClicked) {
                updateStudentData()
            } else {
                saveStudentData()
            }
            clearInput()
        }

        btnClear.setOnClickListener {
            if (lisItemClicked) {
                deleteStudentData()
            }
            clearInput()
        }

        initRecyclerView()

    }

    private fun saveStudentData() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()

        val student = Student(0, name, email)
        viewModel.insertStudent(student)
    }

    @SuppressLint("SetTextI18n")
    private fun updateStudentData() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()

        val student = Student(selectedStudent.id, name, email)
        viewModel.updateStudent(student)

        btnSave.text = "SAVE"
        btnClear.text = "CLEAR"
        lisItemClicked = false
    }

    private fun clearInput() {
        etName.setText("")
        etEmail.setText("")
    }

    @SuppressLint("SetTextI18n")
    private fun deleteStudentData() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()

        val student = Student(selectedStudent.id, name, email)
        viewModel.deleteStudent(student)

        btnSave.text = "SAVE"
        btnClear.text = "CLEAR"
        lisItemClicked = false
    }

    private fun initRecyclerView() {
        rvStudent.layoutManager = LinearLayoutManager(this)
        adapter = StudentRecyclerViewAdapter { selectedItem: Student ->
            listItemClick(selectedItem)
        }
        rvStudent.adapter = adapter

        displayStudentList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayStudentList() {
        viewModel.student.observe(this) {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun listItemClick(student: Student) {
//        Toast.makeText(
//            this, "Clicked ${student.name}",
//            Toast.LENGTH_SHORT
//        ).show()

        selectedStudent = student
        btnSave.text = "UPDATE"
        btnClear.text = "DELETE"
        btnClear.setBackgroundColor(android.graphics.Color.RED)
        lisItemClicked = true

        etName.setText(selectedStudent.name)
        etEmail.setText(selectedStudent.email)

    }
}