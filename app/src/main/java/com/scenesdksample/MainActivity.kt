package com.scenesdksample

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
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
import android.widget.Button



class MainActivity : AppCompatActivity() {

    private var fragment: ArFragment? = null // Ar fragment configures the device's camera for AR
    private var selectedObject: Uri? = null // 3D model that stored in Uri enable android to read it
    private var binding: ActivityMainBinding? = null // responsible for represent activityMain.xml => view
    private var removeObjectButton: Button? = null
    private var resetButton: Button? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize design of activity main page and assign phone camera to Ar camera

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        fragment = supportFragmentManager.findFragmentById(R.id.fragment_camera_preview) as ArFragment?

        //Make a list of usable 3D objects
        initializeAdapterData()

        setupARPlaneTapListener()

        // Display instructions for using app to the users
        AlertDialog.Builder(this)
            .setMessage(Html.fromHtml(resources.getString(R.string.app_help_message), Html.FROM_HTML_MODE_LEGACY))
            .setPositiveButton(resources.getString(android.R.string.ok), null)
            .show()

        // Initialize two buttons Remove previous object , reset

        removeObjectButton = findViewById(R.id.removeObjectButton)

        resetButton = findViewById(R.id.resetButton)

        // Set click listeners for buttons Remove previous object , reset

        removeObjectButton?.setOnClickListener {
            removeSelectedObject()
        }

        resetButton?.setOnClickListener {
            resetScene()
        }
    }

    // Set the tap listener for detecting touches on AR planes
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupARPlaneTapListener() {

        fragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent? ->

            // Check if the tapped plane is horizontal and facing upward
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener // Ignore taps on non-horizontal planes
            }

            // Create an anchor at the hit result's location
            val anchor = hitResult.createAnchor()

            // Place the selected 3D object on the anchor position
            placeSelectedObject(fragment, anchor, selectedObject)
        }
    }


    // Initialize data setup for showing list of objects

    private fun initializeAdapterData() {
        val objectList = mutableListOf<PlaceObjectBean>()

        // Office furniture
        objectList.add(PlaceObjectBean(R.drawable.chair_office, Uri.parse("chair_office.sfb")))
        objectList.add(PlaceObjectBean(R.drawable.table_thumb, Uri.parse("table.sfb")))
        objectList.add(PlaceObjectBean(R.drawable.couch_thumb, Uri.parse("couch.sfb")))

        // Home furniture
        objectList.add(PlaceObjectBean(R.drawable.chair_1, Uri.parse("chair_1.sfb")))
        objectList.add(PlaceObjectBean(R.drawable.sofa_thumb, Uri.parse("sofa.sfb")))
        objectList.add(PlaceObjectBean(R.drawable.fame, Uri.parse("fame.sfb")))
        objectList.add(PlaceObjectBean(R.drawable.fana, Uri.parse("fana.sfb")))
        objectList.add(PlaceObjectBean(R.drawable.bridgett, Uri.parse("bridgett.sfb")))

        // Set the default selection of the object
        selectedObject = objectList[0].uri

        // Set up the adapter and bind it to the RecyclerView
        val objectAdapter = ArItemPlaceObjectAdapter(this, objectList) { placeObject ->
            selectedObject = placeObject.uri
        }
        binding?.rlvImagesPreview?.adapter = objectAdapter
    }

    //This function can be use for render object over camera surface by passing object uri
    /*
    * purpose of this code is to load a 3D model asynchronously and then, upon successful completion,
    *  add the model to the AR scene using the addNodeToScene function.
    *  If there is an exception during the loading process, it displays an error dialog.
    * */

    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeSelectedObject(
        fragment: ArFragment?, //  used to set up and manage the AR session in your Android app
        anchor: Anchor, // fixed orientation in real world (real place)
        model: Uri? //that points to a 3D model or asset
    ) {
        ModelRenderable.builder()
            .setSource(fragment!!.context, model)
            .build()
            .thenAccept { renderable: ModelRenderable ->
                placeRenderableInScene(
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

    //To use for add node (3D object) to scene

    private fun placeRenderableInScene(
        arFragment: ArFragment?,
        anchor: Anchor,
        renderable: Renderable
    ) {
        // Create an AnchorNode with the specified anchor
        val anchorNode = AnchorNode(anchor)

        // Create a TransformableNode to allow user interaction with the object
        val transformableNode = TransformableNode(arFragment!!.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)

        // Set the selected AnchorNode when placing the object
//        selectedAnchorNode = anchorNode

        // Add the AnchorNode to the AR scene
        arFragment.arSceneView.scene.addChild(anchorNode)

        // Select the node to make it interactive
        transformableNode.select()
    }


    // To remove last object added in screen

    private fun removeSelectedObject() {

        val anchorNodes = fragment?.arSceneView?.scene?.children
            ?.filterIsInstance(AnchorNode::class.java)

        // Check if there is a previously added object
        if (anchorNodes?.isNotEmpty() == true) {
            // Remove the last added AnchorNode from the AR scene
            val lastAnchorNode = anchorNodes.last()
            lastAnchorNode.anchor?.detach()
            fragment?.arSceneView?.scene?.removeChild(lastAnchorNode)

            // Display a confirmation dialog or perform any other desired action
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Last object removed")
                .setPositiveButton("OK", null)
                .show()
        } else {
            // Show an AlertDialog if there are no objects to remove
            val builder = AlertDialog.Builder(this)
            builder.setMessage("No objects to remove")
                .setPositiveButton("OK", null)
                .show()
        }
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

            val builder = AlertDialog.Builder(this)
            builder.setMessage("All 3D objects removed")
                .setPositiveButton("OK", null)
                .show()
        } else {

            // Show an AlertDialog if there are no objects to reset

            val builder = AlertDialog.Builder(this)
            builder.setMessage("No 3D objects to reset")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onBackPressed() {

        val intent = Intent(this, StartActivity::class.java)
        startActivity(intent)
        finish()

    }
}
