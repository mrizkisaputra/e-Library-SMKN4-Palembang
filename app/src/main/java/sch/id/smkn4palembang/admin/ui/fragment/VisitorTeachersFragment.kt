package sch.id.smkn4palembang.admin.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.adapter.AdminManagementVisitorsAdapter
import sch.id.smkn4palembang.databinding.FragmentVisitorTeachersBinding
import sch.id.smkn4palembang.model.Visitor
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference

class VisitorTeachersFragment : Fragment() {

    private var _binding: FragmentVisitorTeachersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adminManagementVisitorsAdapter: AdminManagementVisitorsAdapter
    private lateinit var progressDialog: ProgressDialog
    private val listVisitor = ArrayList<Visitor>()
    private val firestore = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitorTeachersBinding.inflate(inflater, container, false)
        return _binding?.root
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

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
            recyclerview.adapter =
                ScaleInAnimationAdapter(adminManagementVisitorsAdapter).apply { setDuration(500) }
            recyclerview.layoutManager = LinearLayoutManager(requireActivity())
            recyclerview.addItemDecoration(
                DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
            )
            recyclerview.setHasFixedSize(true)
        }
    }

    /**
     * @desc fungsi ini untuk mengambil data pengunjung dan menampilkan data terbaru di paling atas
     */
    private fun getVisitors() {
        progressDialog.showProgressDialog()

        firestore.collection(Reference.VISITOR_COLLECTION)
            .whereEqualTo("role", "Guru")
            .orderBy("visiting_time", Query.Direction.DESCENDING)
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
                Toast.makeText(
                    requireActivity(),
                    "Gagal Menampilkan Data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}