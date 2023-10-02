package sch.id.smkn4palembang.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.adapter.AdminManagementVisitorsAdapter
import sch.id.smkn4palembang.databinding.FragmentVisitorTodayBinding
import sch.id.smkn4palembang.model.Visitor
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisitorTodayFragment : Fragment() {
    private var _binding: FragmentVisitorTodayBinding? = null
    private val binding get() = _binding!!
    private val listVisitor = ArrayList<Visitor>()
    private val firestore = Firebase.firestore

    private lateinit var progressDialog: ProgressDialog
    private lateinit var adminManagementVisitorsAdapter: AdminManagementVisitorsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitorTodayBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(requireActivity())

        initRecyclerview()
        getVisitors()
    }

    /**
     * @desc fungsi ini untuk menginisialisasi recyclerview
     */
    private fun initRecyclerview() {
        adminManagementVisitorsAdapter = AdminManagementVisitorsAdapter(requireActivity())
        binding.apply {
            visitorTodayRecyclerview.adapter =
                ScaleInAnimationAdapter(adminManagementVisitorsAdapter).apply { setDuration(500) }
            visitorTodayRecyclerview.layoutManager = LinearLayoutManager(requireActivity())
            visitorTodayRecyclerview.addItemDecoration(
                DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
            )
            visitorTodayRecyclerview.setHasFixedSize(true)
        }
    }

    /**
     * @desc fungsi ini untuk mengambil data pengunjung dan menampilkan data terbaru di paling atas
     */
    private fun getVisitors() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)

        val startOfDay = today.timeInMillis // Waktu mulai hari ini
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) // Waktu akhir hari ini (24 jam setelah waktu mulai)

        progressDialog.showProgressDialog()

        firestore.collection(Reference.VISITOR_COLLECTION)
            .orderBy("time_stamp", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("time_stamp", startOfDay)
            .whereLessThan("time_stamp", endOfDay)
            .get()
            .addOnSuccessListener { result ->
                listVisitor.clear()

                // mengambil semua dokumen
                for (document in result) {
                    val photo = document.getString("photo")
                    val name = document.getString("name")
                    val id = document.getString("id_card")
                    val role = document.getString("role")
                    val timeStamp = document.getLong("time_stamp")
                    val visitingTime = document.getString("visiting_time")

                    val visitor = Visitor(
                        photo = photo, name = name, id = id, role = role,
                        visitingTime = visitingTime, timetamp = timeStamp
                    )
                    listVisitor.add(visitor)
                }

                // Setelah mengambil dan mengurutkan data, update RecyclerView
                adminManagementVisitorsAdapter.setData(listVisitor)
                adminManagementVisitorsAdapter.notifyDataSetChanged()
                progressDialog.dismissProgressDialog()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismissProgressDialog()
            }
    }


}