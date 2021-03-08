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

import android.content.Intent;
import android.lib.widget.snackbar.Snackbar;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cw.artest.R;
import com.cw.artest.video.PlayPickActivity;
import com.cw.artest.video.SmartPickVideo;
import com.cw.artest.video.VideoOnPrepareCallBack;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
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

    // The color to filter out of the video.
    private static final Color CHROMA_KEY_COLOR = new Color(0.1843f, 1.0f, 0.098f);

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, Node> augmentedImageMap = new HashMap<>();

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
                        int mode = getIntent().getIntExtra("showType", 0);
                        if (mode == 0) {
                            //模型展示
                            AugmentedImageNode node = new AugmentedImageNode(this);
                            node.setImage(arFragment.getTransformationSystem(), augmentedImage);
                            augmentedImageMap.put(augmentedImage, node);
                            this.arFragment.getArSceneView().getScene().addChild(node);
                        } else if (mode == 1) {
                            //图片展示
                            AnchorNode node = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                            ViewRenderable.builder()
                                    .setView(this, R.layout.renderable_image).build()
                                    .thenAccept(viewRenderable -> {
                                        ImageView imageView = viewRenderable.getView().findViewById(R.id.image);
                                        Glide.with(this).load("file:///android_asset/earth.gif").into(imageView);
                                        //TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                                        //AnchorNode transformableNode = new AnchorNode();
                                        Node transformableNode = new Node();
                                        transformableNode.setParent(node);
                                        transformableNode.setLocalRotation(Quaternion.eulerAngles(new Vector3(-90, 0, 0)));
                                        Vector3 localPosition = transformableNode.getLocalPosition();
                                        //transformableNode.setAnchor(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                                        transformableNode.setLocalPosition(new Vector3(localPosition.x, localPosition.y, localPosition.z + 0.5f * augmentedImage.getExtentZ()));
                                        transformableNode.setRenderable(viewRenderable);
                                    }).exceptionally(throwable -> {
                                Snackbar.with(this).setText("模型创建失败").show();
                                return null;
                            });
                            augmentedImageMap.put(augmentedImage, node);
                            this.arFragment.getArSceneView().getScene().addChild(node);
                        } else if (mode == 2) {
                            //视频展示
                            AnchorNode node = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));

                            // Create an ExternalTexture for displaying the contents of the video.
                            ExternalTexture texture = new ExternalTexture();

                            ViewRenderable.builder()
                                    .setView(this, R.layout.renderable_video).build()
                                    .thenAccept(viewRenderable -> {
                                        SmartPickVideo smartPickVideo = viewRenderable.getView().findViewById(R.id.video_player);
                                        smartPickVideo.setUp("http://vjs.zencdn.net/v/oceans.mp4", true, "");

                                        smartPickVideo.startPlayLogic();
                                        smartPickVideo.setVideoAllCallBack(new VideoOnPrepareCallBack() {
                                            @Override
                                            public void onPrepared(String url, Object... objects) {
                                                smartPickVideo.setDisplay(texture.getSurface());
                                            }
                                        });
                                        Node transformableNode = new Node();
                                        transformableNode.setParent(node);
                                        transformableNode.setLocalScale(new Vector3(0.2f, 0.2f, 1f));
                                        transformableNode.setRenderable(viewRenderable);

                                        ModelRenderable.builder()
                                                .setSource(this, Uri.parse("models/chroma_key_video.sfb")).build()
                                                .thenAccept(modelRenderable -> {
                                                    modelRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                                                    modelRenderable.getMaterial().setFloat4("keyColor", CHROMA_KEY_COLOR);

                                                    Node videoNode = new Node();
                                                    videoNode.setParent(node);
                                                    //这里缩放需要计算一下，目前写死
                                                    videoNode.setLocalPosition(new Vector3(0, 0.062f, -0.00001f));
                                                    videoNode.setLocalScale(new Vector3(0.271f, 0.115f, 1f));

                                                    videoNode.setRenderable(modelRenderable);
                                                }).exceptionally(throwable -> {
                                            Snackbar.with(this).setText("模型创建失败").show();
                                            return null;
                                        });
                                    }).exceptionally(throwable -> {
                                Snackbar.with(this).setText("模型创建失败").show();
                                return null;
                            });
                            augmentedImageMap.put(augmentedImage, node);
                            this.arFragment.getArSceneView().getScene().addChild(node);
                        } else if (mode == 3) {
                            //视频展示(透明视频)
                            AnchorNode node = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));

                            // Create an ExternalTexture for displaying the contents of the video.
                            ExternalTexture texture = new ExternalTexture();

                            // Create an Android MediaPlayer to capture the video on the external texture's surface.
                            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.lion_chroma);
                            mediaPlayer.setSurface(texture.getSurface());
                            mediaPlayer.setLooping(true);

                            ModelRenderable.builder()
                                    .setSource(this, Uri.parse("models/chroma_key_video.sfb")).build()
                                    .thenAccept(modelRenderable -> {
                                        modelRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                                        modelRenderable.getMaterial().setFloat4("keyColor", CHROMA_KEY_COLOR);

                                        Node transformableNode = new Node();
                                        transformableNode.setParent(node);
                                        transformableNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
                                        transformableNode.setRenderable(modelRenderable);

                                        if (!mediaPlayer.isPlaying()) {
                                            mediaPlayer.start();
                                        }
                                    }).exceptionally(throwable -> {
                                Snackbar.with(this).setText("模型创建失败").show();
                                return null;
                            });
                            augmentedImageMap.put(augmentedImage, node);
                            this.arFragment.getArSceneView().getScene().addChild(node);
                        } else if (mode == 4) {
                            //视频展示（不跟踪物体）
                            Intent intent = new Intent(this, PlayPickActivity.class);
                            intent.putExtra(PlayPickActivity.VIDEO_COVER, "https://img-blog.csdnimg.cn/20190301125255914.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MTAxMDE5OA==,size_16,color_FFFFFF,t_70");
                            intent.putExtra(PlayPickActivity.VIDEO_URL, "http://vjs.zencdn.net/v/oceans.mp4");
                            intent.putExtra(PlayPickActivity.VIDEO_AUTO_PLAY, true);
                            startActivityForResult(intent, 0);
                            augmentedImageMap.put(augmentedImage, new Node());
                        }
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
        int mode = getIntent().getIntExtra("showType", 0);
        Intent intent = new Intent(this, AugmentedImageActivity.class);
        intent.putExtra("showType", mode);
        startActivity(intent);
    }
}
