package tv.formuler.mytvonline.technic.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.room.Room
import kotlinx.coroutines.*
import tv.formuler.mytvonline.R
import tv.formuler.mytvonline.technic.data.Class
import tv.formuler.mytvonline.technic.data.School
import tv.formuler.mytvonline.technic.TecDatabase
import tv.formuler.mytvonline.technic.data.User

class LoginDialog(val dismissListener: DialogInterface.OnDismissListener) : DialogFragment() {

    lateinit var schoolSpinner: Spinner
    lateinit var classSpinner: Spinner

    val schoolMap: HashMap<String, School> = HashMap<String, School>()
    val classMap: HashMap<String, Class> = HashMap<String, Class>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val inflater = requireActivity().layoutInflater;

            val dialogView = inflater.inflate(R.layout.dialog_login, null)

            schoolSpinner = dialogView.findViewById(R.id.spinner_school) as Spinner
            classSpinner = dialogView.findViewById(R.id.spinner_class) as Spinner

            val dialog = AlertDialog.Builder(it).setView(dialogView)
                .setTitle("Login")
                .setPositiveButton("OK", null)
                .create()

            dialog.setOnShowListener {
                // ok button 눌렀을 경우 바로 dismiss 시키지 않기 위해서
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.setOnClickListener {
                    val selectedSchoolItemPosition = schoolSpinner.selectedItemPosition
                    val selectedClassItemPosition = classSpinner.selectedItemPosition
                    val number = dialogView.findViewById<EditText>(R.id.et_number).text
                    val name = dialogView.findViewById<EditText>(R.id.et_name).text

                    if (selectedSchoolItemPosition == 0 || selectedClassItemPosition == 0
                        || TextUtils.isEmpty(number) || TextUtils.isEmpty(name)) {
                        Toast.makeText(activity, "Please check login items", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val selectedSchool = schoolSpinner.getItemAtPosition(selectedSchoolItemPosition) as School
                    val selectedClass = classSpinner.getItemAtPosition(selectedClassItemPosition) as Class

                    TecAPIManager()
                        .requestRegister(
                            selectedSchool.sId,
                            selectedClass.cId,
                            number.toString(),
                            name.toString()
                        )
                        .onResponse(object : OnResponse<User> {
                            override fun onResponse(user: User?) {
                                user?.id?.let {
                                    val db = Room.databaseBuilder(
                                        activity!!.applicationContext,
                                        TecDatabase::class.java,
                                        "TecDatabase"
                                    ).build()
                                    GlobalScope.launch {
                                        val u = db.userDao().getUser(user.id)
                                        if (u == null) {
                                            db.userDao().insertUser(user)
                                            dismiss()
                                        } else {
                                            activity?.runOnUiThread {
                                                Toast.makeText(
                                                    activity,
                                                    "The user that exists",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } ?: {
                                    Toast.makeText(activity, "No user", Toast.LENGTH_SHORT).show()
                                }()
                            }
                        })
                        .onFailure(object : OnFailure {
                            override fun onFailure(code: Int, message: String?) {
                                Toast.makeText(activity, "Fail the login", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }).request()
                }
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        val schools = ArrayList<School>()
        schools.add(School("-1", "학교선택", "hint"))
        val schoolSpinAdapter = SchoolSpinAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            schools
        )

        schoolSpinner.adapter = schoolSpinAdapter
        schoolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val classSpinnerAdapter = classSpinner.adapter as ClassSpinAdapter
                classSpinnerAdapter.removeAll()
                classSpinnerAdapter.add(Class("-1", "학급선택", "hint"))

                if (position != 0) {
                    val school = schoolSpinAdapter.getItem(position)
                    Log.d("LoginDialog", school.toString())
                    TecAPIManager()
                        .requestClasses(school!!.sId)
                        .onResponse(object : OnResponse<List<Class>> {
                            override fun onResponse(response: List<Class>?) {
                                response?.forEach {
                                    classSpinnerAdapter.add(it)
                                    classMap.put(it.cId, it)
                                }
                                classSpinner.setSelection(0)
                            }
                        }).request()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        TecAPIManager()
            .requestSchools()
            .onResponse(object : OnResponse<List<School>> {
                override fun onResponse(response: List<School>?) {
                    response?.forEach {
                        schoolSpinAdapter.add(it)
                        schoolMap.put(it.sId, it)
                    }
                }
            }).request()


        val classes = ArrayList<Class>()
        classes.add(Class("-1", "학급선택", "hint"))
        val classSpinAdapter = ClassSpinAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            classes
        )

        classSpinner.adapter = classSpinAdapter
        classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val clazz = classSpinAdapter.getItem(position)
                    Log.d("LoginDialog", clazz.toString())
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroy() {
        dismissListener.onDismiss(dialog)
        super.onDestroy()
    }
}

class SchoolSpinAdapter(context: Context, resource: Int, schools: List<School>) : ArrayAdapter<School>(
    context,
    resource,
    schools
) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label: TextView = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        label.setText(item!!.name)
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val label: TextView = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        label.setText(item!!.name)
        return label
    }
}

class ClassSpinAdapter(context: Context, resource: Int, classes: List<Class>) : ArrayAdapter<Class>(
    context,
    resource,
    classes
) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label: TextView = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        label.setText(item!!.name)
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val label: TextView = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        label.setText(item!!.name)
        return label
    }

    fun removeAll() {
        for (i: Int in count - 1 downTo 0) {
            remove(getItem(i))
        }
    }
}