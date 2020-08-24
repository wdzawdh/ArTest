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

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";

    // The augmented image represented by this node.
    private AugmentedImage image;
    private CompletableFuture<ModelRenderable> andy;
    private ModelRenderable renderable;
    private ModelAnimator animator;
    private int nextAnimation;
    private float x;
    private float y;

    public AugmentedImageNode(Context context) {
        // Upon construction, start loading the models for the corners of the frame.
        if (andy == null) {
            andy = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/andy_dance.sfb"))
                    .build()
                    .thenApply(renderable -> this.renderable = renderable);
        }
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(TransformationSystem transformationSystem, AugmentedImage image) {
        this.image = image;

        if (!andy.isDone()) {
            CompletableFuture.allOf(andy)
                    .thenAccept((Void aVoid) -> setImage(transformationSystem, image))
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                    });
        }

        setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));

        TransformableNode andy = new TransformableNode(transformationSystem);
        andy.setParent(this);
        andy.setRenderable(renderable);
        andy.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    x = motionEvent.getX();
                    y = motionEvent.getY();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getX() - x < 1 && motionEvent.getY() - y < 1) {
                        onPlayAnimation();
                    }
                }
                return false;
            }
        });

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));
    }

    public void onPlayAnimation() {
        if (animator == null || !animator.isRunning()) {
            AnimationData data = renderable.getAnimationData(nextAnimation);
            nextAnimation = (nextAnimation + 1) % renderable.getAnimationDataCount();
            animator = new ModelAnimator(data, renderable);
            animator.start();
        }
    }

    public AugmentedImage getImage() {
        return image;
    }
}
