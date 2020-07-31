/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cw.artest.augmentedimage;

import android.lib.widget.snackbar.Snackbar;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cw.artest.R;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * ArAugmentedImage_getTrackingMethod() and render only when the tracking method equals to
 * AR_AUGMENTED_IMAGE_TRACKING_METHOD_FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/c/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public class AugmentedImageActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ImageView fitToScanView;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AnchorNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augment_image);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    //Snackbar.with(this).setText("检测到图像").show();
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        boolean mode = getIntent().getBooleanExtra("mode", true);
                        if (mode) {
                            AugmentedImageNode node = new AugmentedImageNode(this);
                            node.setImage(augmentedImage);
                            augmentedImageMap.put(augmentedImage, node);
                            this.arFragment.getArSceneView().getScene().addChild(node);
                        } else {
                            AnchorNode node = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                            ViewRenderable.builder().setView(this, R.layout.renderable_image).build()
                                    .thenAccept(viewRenderable -> {
                                        ImageView imageView = viewRenderable.getView().findViewById(R.id.image);
                                        Glide.with(this).load("file:///android_asset/earth.gif").into(imageView);
                                        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                                        transformableNode.setParent(node);
                                        transformableNode.setLocalRotation(Quaternion.eulerAngles(new Vector3(-90, 0, 0)));
                                        Vector3 localPosition = transformableNode.getLocalPosition();

                                        transformableNode.setLocalPosition(new Vector3(localPosition.x, localPosition.y, localPosition.z + 0.5f * augmentedImage.getExtentZ()));
                                        transformableNode.setRenderable(viewRenderable);
                                    }).exceptionally(throwable -> {
                                Snackbar.with(this).setText("模型创建失败").show();
                                return null;
                            });
                            augmentedImageMap.put(augmentedImage, node);
                            this.arFragment.getArSceneView().getScene().addChild(node);
                        }
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }
}
