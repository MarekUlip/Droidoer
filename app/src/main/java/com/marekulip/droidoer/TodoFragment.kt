package com.marekulip.droidoer

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.*
import android.widget.EditText
import com.marekulip.droidoer.database.DroidoerDatabase
import com.marekulip.droidoer.database.MainTask
import com.marekulip.droidoer.database.SubTask

private const val CATEGORY = "category"

/**
 * A simple [Fragment] subclass.
 * Use the [TodoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodoFragment : Fragment(), MyTodoRecyclerViewAdapter.Callback {
    var category = 0
    set(value) {
        field = value
        mAdapter?.category = value
        reloadAdapterItems()
    }
    var isDisplayingCompleted = false
    set(value){
        field = value
        reloadAdapterItems()
    }
    var mainTaskHolder: MainTask? = null
    var subTaskHolder: SubTask? = null
    var database: DroidoerDatabase? = null

    private var listener: OnFragmentInteractionListener? = null
    private var listItems: MutableList<MainTask> = ArrayList()
    private var mAdapter: MyTodoRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getInt(CATEGORY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val recyclerView = inflater.inflate(R.layout.list_layout,container,false)
        listItems = loadTasks()
        mAdapter = MyTodoRecyclerViewAdapter(listItems,context as Context,this)
        if(recyclerView is RecyclerView){
            with(recyclerView){
                adapter = mAdapter
                layoutManager = LinearLayoutManager(context)
            }
            recyclerView.setOnCreateContextMenuListener { menu, v, menuInfo ->
                menu.add(0,R.id.action_rename,0,"Rename")
                menu.add(0,R.id.action_mark_complete,0,"Mark as completed")
                menu.add(0,R.id.action_delete,0,"Delete")
            }
            registerForContextMenu(recyclerView)
        }
        return recyclerView
    }

    override fun onCategoryChange(subTask: SubTask, category: Int, position: Int) {
        subTask.category = category
        database?.subTaskDataDao()?.updateAll(subTask)
        updateMainItem(subTask.mainTaskId,position)
    }

    override fun onSubTaskAdding(mainTask: MainTask) {
        createSubDialog(true,null,mainTask.id as Long)
    }

    fun addMainTask(){
        createMainDialog(true,null)
    }

    override fun setMainTaskHldr(mainTask: MainTask) {
        mainTaskHolder = mainTask
    }

    override fun setSubTaskHldr(subTask: SubTask) {
        subTaskHolder = subTask
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        /*if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }*/
        database = DroidoerDatabase.getInstance(context)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_rename -> createMainDialog(false,mainTaskHolder?.name)
            R.id.action_delete -> deleteTask()
            R.id.action_mark_complete -> changeTaskCompletionStat(true)
            R.id.action_mark_active -> changeTaskCompletionStat(false)
            R.id.action_rename_sub -> createSubDialog(false,subTaskHolder?.description,subTaskHolder?.mainTaskId as Long)
            R.id.action_delete_sub -> deleteSubTask()
            else -> return false

        }
        return true
    }

    private fun createMainDialog(isNew:Boolean, text:String?){
        createAlertDialog(true,isNew,text,null)
    }

    private fun createSubDialog(isNew:Boolean, text:String?, mainTaskId: Long){
        createAlertDialog(false,isNew,text,mainTaskId)
    }

    private fun loadTasks():MutableList<MainTask>{
        val mainItems = if(isDisplayingCompleted) {
            database?.mainTaskDataDao()?.getSome(true)
        } else{
            database?.mainTaskDataDao()?.getSome(false)
        }
        if(mainItems is MutableList<MainTask>){
            val subItems = when(category) {
                0,1,2,3 -> database?.subTaskDataDao()?.getAllByCat(category)
                else -> database?.subTaskDataDao()?.getAll()
            }
            val subItemsMap = HashMap<Long, MutableList<SubTask>>()
            if(subItems is MutableList<SubTask>){
                for(item in subItems){
                    if(!subItemsMap.containsKey(item.mainTaskId)) {
                        subItemsMap[item.mainTaskId] = ArrayList()
                    }
                    subItemsMap[item.mainTaskId]?.add(item)
                }
            }
            for(item in mainItems){
                var items = subItemsMap[item.id]
                if(items == null)
                {
                    items = ArrayList()
                }
                item.subTasks = items
            }
        }
        return mainItems ?: ArrayList()
    }

    private fun loadTask(id:Long):MainTask{
        val mainItem = database?.mainTaskDataDao()?.findById(id)
        val subItems = when(category) {
            0,1,2,3 -> database?.subTaskDataDao()?.getAllByMainTaskAndCat(id,category)
            else -> database?.subTaskDataDao()?.getAllByMainTask(id)
        }
        if(subItems is MutableList<SubTask>) {
            mainItem?.subTasks = subItems
        }else{
            mainItem?.subTasks = ArrayList()
        }
        return mainItem as MainTask
    }

    private fun createAlertDialog(isMainTask: Boolean, isNew: Boolean, text: String?, mainTaskId: Long?){
        val builder = AlertDialog.Builder(context)
        val editText = EditText(context)
        builder.setView(editText)
        editText.requestFocus()
        editText.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        if (isMainTask) {
            builder.setTitle("Main task name")
        } else{
            builder.setTitle("Sub task description")
        }
        if(isNew) {
            editText.hint = "Task name"
            builder.setPositiveButton("Create"){ dialog, _ ->
                if (isMainTask){
                    val mainTask = MainTask()
                    mainTask.completed = false
                    mainTask.name = editText.text.toString()
                    database?.mainTaskDataDao()?.insertAll(mainTask)
                    reloadAdapterItems()
                } else{
                    val subTask = SubTask()
                    subTask.category = if (this.category == 4) 0 else this.category
                    subTask.description = editText.text.toString()
                    subTask.mainTaskId = mainTaskId as Long
                    database?.subTaskDataDao()?.insertAll(subTask)
                    updateMainItem(subTask.mainTaskId,null)
                }
                dialog.dismiss()
            }
        } else {
            editText.text.insert(0,text)
            builder.setPositiveButton("Rename"){ dialog, _ ->
                val id: Long
                if (isMainTask){
                    mainTaskHolder?.name = editText.text.toString()
                    database?.mainTaskDataDao()?.updateAll(mainTaskHolder as MainTask)
                    id = mainTaskHolder?.id as Long
                } else{
                    subTaskHolder?.description = editText.text.toString()
                    database?.subTaskDataDao()?.updateAll(subTaskHolder as SubTask)
                    id = subTaskHolder?.mainTaskId as Long
                }
                updateMainItem(id,null)
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("Cancel"){dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun updateMainItem(id:Long, position: Int?){
        val pos = position ?: findMainTaskById(id)
        listItems[pos] = loadTask(id)
        mAdapter?.notifyItemChanged(pos)
    }

    private fun reloadAdapterItems(){
        listItems = loadTasks()
        mAdapter?.mValues = listItems
        mAdapter?.notifyDataSetChanged()
    }

    private fun findMainTaskById(id:Long): Int{
        for((index, value) in listItems.withIndex()){
            if(value.id == id){
                return index
            }
        }
        return -1
    }

    private fun deleteTask(){
        if (mainTaskHolder!=null) {
            database?.mainTaskDataDao()?.deleteAll(mainTaskHolder as MainTask)
            reloadAdapterItems()
        }
    }

    private fun changeTaskCompletionStat(completed: Boolean){
        if(mainTaskHolder != null) {
            mainTaskHolder?.completed = completed
            database?.mainTaskDataDao()?.updateAll(mainTaskHolder as MainTask)
            reloadAdapterItems()
        }
    }

    private fun deleteSubTask(){
        if (subTaskHolder!=null) {
            database?.subTaskDataDao()?.deleteAll(subTaskHolder as SubTask)
            updateMainItem(subTaskHolder?.mainTaskId as Long,null)
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * @param category Subtasks category.
         * @return A new instance of fragment TodoFragment.
         */
        @JvmStatic
        fun newInstance(category: Int) =
                TodoFragment().apply {
                    arguments = Bundle().apply {
                        putInt(CATEGORY, category)
                    }
                }
    }

    inner class WorkerThread(db: DroidoerDatabase): Thread(){

    }
}
