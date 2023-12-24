package com.scenesdksample

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.scenesdksample.adapter.ArItemPlaceObjectAdapter
import com.scenesdksample.databinding.ActivityMainBinding
import com.scenesdksample.model.PlaceObjectBean
import android.provider.MediaStore
import android.widget.Button



class MainActivity : AppCompatActivity() {

    private var fragment: ArFragment? = null
    private var selectedObject: Uri? = null
    private var binding: ActivityMainBinding? = null
    private var selectedAnchorNode: AnchorNode? = null


    //
    private var removeObjectButton: Button? = null
    private var resetButton: Button? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        registerCameraOnTapListener()
        showAppUsageAlert()

        // Initialize buttons
        removeObjectButton = findViewById(R.id.removeObjectButton)
        resetButton = findViewById(R.id.resetButton)

        // Set click listeners for buttons
        removeObjectButton?.setOnClickListener {
            removeSelectedObject()
        }

        resetButton?.setOnClickListener {
            resetScene()
        }
    }

    //Initial setup for View
    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        fragment =
            supportFragmentManager.findFragmentById(R.id.fragment_camera_preview) as ArFragment?
        initAdapterData()
    }

    //Registered scene tapped listener
    @RequiresApi(Build.VERSION_CODES.N)
    private fun registerCameraOnTapListener() {
        fragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent? ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            placeObject(fragment, anchor, selectedObject)
        }
    }

    //App usage step dialog
    @RequiresApi(Build.VERSION_CODES.N)
    private fun showAppUsageAlert() {
        AlertDialog.Builder(this)
            .setMessage(fromHtml(resources.getString(R.string.app_help_message)))
            .setPositiveButton(resources.getString(android.R.string.ok), null)
            .show()
    }

    //Initial data setup for showing list of objects
    private fun initAdapterData() {
        val list = mutableListOf<PlaceObjectBean>()

//        Office furniture

        list.add(PlaceObjectBean(R.drawable.chair_office, Uri.parse("chair_office.sfb")))
        list.add(PlaceObjectBean(R.drawable.table_thumb, Uri.parse("table.sfb")))
        list.add(PlaceObjectBean(R.drawable.chair_thumb, Uri.parse("chair.sfb")))
        list.add(PlaceObjectBean(R.drawable.couch_thumb, Uri.parse("couch.sfb")))
        list.add(PlaceObjectBean(R.drawable.chair_1, Uri.parse("chair_1.sfb")))


//        Home furniture

        list.add(PlaceObjectBean(R.drawable.sofa_thumb, Uri.parse("sofa.sfb")))
        list.add(PlaceObjectBean(R.drawable.fame, Uri.parse("fame.sfb")))
        list.add(PlaceObjectBean(R.drawable.fana, Uri.parse("fana.sfb")))
        list.add(PlaceObjectBean(R.drawable.bridgett, Uri.parse("bridgett.sfb")))

        //set default selection of object
        selectedObject = list[0].uri

        val userAdapter = ArItemPlaceObjectAdapter(this, list) { placeObject ->
            selectedObject = placeObject.uri
        }
        binding?.rlvImagesPreview?.adapter = userAdapter
    }

    //This function can be use for render object over camera surface by passing object uri
    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeObject(
        fragment: ArFragment?,
        anchor: Anchor,
        model: Uri?
    ) {
        ModelRenderable.builder()
            .setSource(fragment!!.context, model)
            .build()
            .thenAccept { renderable: ModelRenderable ->
                addNodeToScene(
                    fragment,
                    anchor,
                    renderable
                )
            }
            .exceptionally { throwable: Throwable ->
                val builder =
                    AlertDialog.Builder(this)
                builder.setMessage(throwable.message)
                    .setTitle(resources.getString(R.string.error))
                val dialog = builder.create()
                dialog.show()
                null
            }


    }

    //To use for add node to scene
    private fun addNodeToScene(
        fragment: ArFragment?,
        anchor: Anchor,
        renderable: Renderable
    ) {
        val anchorNode =
            AnchorNode(anchor)
        val node = TransformableNode(fragment!!.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        // Set the selected AnchorNode when placing the object
        selectedAnchorNode = anchorNode
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    //To use show string content in html form
    @RequiresApi(Build.VERSION_CODES.N)
    private fun fromHtml(source: String?): Spanned? {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    }

    override fun onBackPressed() {
        exitConfirmationAlert()
    }

    //To use show confirmation dialog for exit app
    private fun exitConfirmationAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.are_you_sure_want_to_exit))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ -> this@MainActivity.finish() }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    // Other methods...

    private fun removeSelectedObject() {
        // Implement logic to remove the selected 3D object from the scene
        // For example, you can remove the last added node:
        if (selectedAnchorNode != null){
            val lastAnchorNode = getLastAnchorNode()
            lastAnchorNode?.anchor?.detach()
            lastAnchorNode?.removeChild(lastAnchorNode.children.firstOrNull())
            selectedAnchorNode = null

            // Display a confirmation dialog or perform any other desired action
            showObjectRemovedDialog()

        } else {
            // Show an AlertDialog if there is no object to remove
            showNoObjectToRemoveDialog()
        }

    }
    private fun showObjectRemovedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Object removed")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showNoObjectToRemoveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Remove last object only")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getLastAnchorNode(): AnchorNode? {
        // Implement logic to get the last added AnchorNode
        // For example, you can iterate through the scene's children
        // and find the last AnchorNode.

        return fragment?.arSceneView?.scene?.children
            ?.filterIsInstance(AnchorNode::class.java)
            ?.lastOrNull()
    }


    private fun resetScene() {
        val anchorNodes = fragment?.arSceneView?.scene?.children
            ?.filterIsInstance(AnchorNode::class.java)

        // Check if there are any AnchorNodes
        if (anchorNodes?.isNotEmpty() == true) {
            // Remove all AnchorNodes from the AR scene
            anchorNodes.forEach { anchorNode ->
                anchorNode.anchor?.detach()
                fragment?.arSceneView?.scene?.removeChild(anchorNode)
            }

            // Display a confirmation dialog
            showResetConfirmationDialog()
        } else {
            // Show an AlertDialog if there are no objects to reset
            showNoObjectsToResetDialog()
        }
    }

    private fun showResetConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("All 3D objects removed")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showNoObjectsToResetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("No 3D objects to reset")
            .setPositiveButton("OK", null)
            .show()
    }

}