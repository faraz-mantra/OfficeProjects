package com.nowfloats.Analytics_Screen.Search_Query_Adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nowfloats.Analytics_Screen.VmnMediaPlayer;
import com.nowfloats.Analytics_Screen.model.VmnCallModel;
import com.nowfloats.util.Methods;
import com.thinksity.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Admin on 27-04-2017.
 */

public class VmnCallAdapter extends BaseExpandableListAdapter {


    private final Handler handler;
    private ArrayList<ArrayList<VmnCallModel>> listData;
    private Context mContext;
    private ConnectToVmnPlayer connectToVmn;
    private String currentPlay="-1 -1";
    public VmnCallAdapter(Context context,ArrayList<ArrayList<VmnCallModel>> hashMap){
        mContext = context;
        listData = hashMap;
        Log.v("ggg",listData.size()+" ");
        handler = new Handler();
        connectToVmn = new ConnectToVmnPlayer();
    }

    @Override
    public int getGroupCount() {
        return listData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listData.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listData.get(groupPosition).get(0).getCallerNumber();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        MyParentHolder parentHolder;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_vmn_call_item, parent, false);
            parentHolder = new MyParentHolder(convertView);
            convertView.setTag(parentHolder);

        }else{
            parentHolder = (MyParentHolder) convertView.getTag();
        }
        parentHolder.callerNumber.setText((String)getGroup(groupPosition));

        parentHolder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCall((String)getGroup(groupPosition));
            }
        });

        return convertView;
    }

    private void startCall(String number) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:"+number));
        mContext.startActivity(Intent.createChooser(callIntent,"Call by:"));
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final MyChildHolder childHolder;
        final VmnCallModel childModel = (VmnCallModel) getChild(groupPosition, childPosition);
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vmn_call_child_item, parent, false);
            childHolder = new MyChildHolder(convertView);
            convertView.setTag(childHolder);

        }else{
           childHolder = (MyChildHolder) convertView.getTag();
        }

        childHolder.date.setText(connectToVmn.getDate(Methods.getFormattedDate(childModel.getCallDateTime())));
        if(childModel.getCallStatus().equalsIgnoreCase("MISSED")){
            childHolder.callImage.setImageResource(R.drawable.ic_call_missed);
            childHolder.play.setText("Missed Call");
            childHolder.play.setTextColor(ContextCompat.getColor(mContext,R.color.gray_transparent));
            childHolder.play.setPaintFlags(childHolder.play.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
        }else
        {
            String[] positions = currentPlay.split(" ");
            if(Integer.parseInt(positions[0]) == groupPosition && Integer.parseInt(positions[1]) == childPosition){
                childHolder.play.setTextColor(ContextCompat.getColor(mContext,R.color.gray_transparent));
            }else{
                childHolder.play.setTextColor(ContextCompat.getColor(mContext,R.color.gray));
            }
            childHolder.callImage.setImageResource(R.drawable.ic_call_received);
            childHolder.play.setText(mContext.getString(R.string.play_with_underline));
            childHolder.play.setPaintFlags(childHolder.play.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            childHolder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPlay = groupPosition+" "+childPosition;
                    childHolder.play.setTextColor(ContextCompat.getColor(mContext,R.color.gray_transparent));
                    connectToVmn.setData(childModel);
                }
            });

        }

        return convertView;
    }

    public class ConnectToVmnPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, View.OnClickListener {
        VmnMediaPlayer vmnMediaPlayer;
        MediaHolder childHolder;
        MaterialDialog mediaDialog;
        VmnCallModel mediaData;

        private ConnectToVmnPlayer(){
            vmnMediaPlayer = VmnMediaPlayer.getInstance(mContext);
            vmnMediaPlayer.setUpPlayer(this);
            setPlayerDialog();
        }

        private void releaseResources(){
            vmnMediaPlayer.release();
            vmnMediaPlayer = null;
        }

        private void setData(VmnCallModel model){
            mediaData = model;
            showDialog();
        }


        private String getDate(String date){
            return date.replaceAll(",.*?at","");
        }
        private MaterialDialog setPlayerDialog() {
            View parent = LayoutInflater.from(mContext).inflate(R.layout.dialog_media_player,null,false);
            childHolder = new MediaHolder(parent);
            childHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser)
                        vmnMediaPlayer.seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            childHolder.playButton.setOnClickListener(this);
            childHolder.downloadImage.setOnClickListener(this);

            mediaDialog = new MaterialDialog.Builder(mContext)
                    .customView(parent,false)
                    .cancelable(false)
                    .negativeColorRes(R.color.primary_color)
                    .negativeText(mContext.getString(R.string.call))
                    .positiveColorRes(R.color.gray_transparent)
                    .positiveText(mContext.getString(R.string.cancel))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            dialog.dismiss();
                            vmnMediaPlayer.stop();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            pause();
                            startCall(mediaData.getCallerNumber());
                        }
                    })
                    .build();
            return mediaDialog;
        }

        private void showDialog(){
            resetMediaPlayer();
            if(mediaDialog == null) {
                mediaDialog = setPlayerDialog();
            }
            mediaDialog.show();
            vmnMediaPlayer.setDataUrl(mediaData.getCallRecordingUri());
        }
        private void resetMediaPlayer(){
            childHolder.playButton.setImageResource(R.drawable.ic_pause_yellow);
            childHolder.date.setText(getDate(Methods.getFormattedDate(mediaData.getCallDateTime())));
            childHolder.seekBar.setProgress(0);
            childHolder.recCurrPoint.setText("0:00");
            childHolder.recEndPoint.setText(getTimeFromMilliSeconds(mediaData.getCallDuration()));
        }

        Runnable updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if(vmnMediaPlayer == null){
                    return;
                }
                int duration=vmnMediaPlayer.getCurrentPosition();
                int seekBarPos = seekBarPos(duration);
                String time=getTimeFromMilliSeconds(duration);
                if(duration == vmnMediaPlayer.getDuration()){
                    return;
                }else if(!isPlaying()){
                    childHolder.seekBar.setProgress(seekBarPos);
                    childHolder.recCurrPoint.setText(time);
                    return;
                }
                Log.v("ggg",duration+" "+time);
                childHolder.seekBar.setProgress(seekBarPos);
                childHolder.recCurrPoint.setText(time);
                handler.postDelayed(updateSeekBar,1000);
            }
        };

        int seekBarPos(int duration){
            return duration*100 / vmnMediaPlayer.getDuration();
        }

        private boolean isPlaying(){
            return vmnMediaPlayer.isPlaying();
        }

        private String getTimeFromMilliSeconds(int pos) {
            int seconds = (pos / 1000) % 60 ;
            int minutes = ((pos / (1000*60)) % 60);
            return String.format(Locale.ENGLISH,"%d:%02d", minutes,seconds);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            childHolder.seekBar.setProgress(0);
            childHolder.recCurrPoint.setText("0:00");
            childHolder.playButton.setImageResource(R.drawable.ic_play_arrow);
            Log.v("ggg","complete");
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            start();
        }

        private void pause(){
            vmnMediaPlayer.pause();
        }
        private void start() {
            vmnMediaPlayer.start();
            vmnMediaPlayer.seekTo(childHolder.seekBar.getProgress());
            handler.postDelayed(updateSeekBar,1000);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imgview_play:
                    if(isPlaying())
                    {
                        pause();
                        childHolder.playButton.setImageResource(R.drawable.ic_play_arrow);
                    }else
                    {
                        start();
                        childHolder.playButton.setImageResource(R.drawable.ic_pause_yellow);
                    }
                    break;
                case R.id.download:
                    PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(mContext,R.color.gray_transparent), PorterDuff.Mode.SRC_IN);
                    childHolder.downloadImage.setColorFilter(porterDuffColorFilter);
                    Toast.makeText(mContext, mContext.getString(R.string.downloading), Toast.LENGTH_SHORT).show();
                    if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                downloadFile(mediaData.getCallRecordingUri(),mediaData.getCallerNumber(),childHolder.downloadImage);
                            }
                        }).start();
                    }else{
                        ((RequestPermission)mContext).reuestStoragePermission();
                    }
                    break;

            }
        }
    }
    public void releaseResources(){
        connectToVmn.releaseResources();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    private class MyParentHolder {

        TextView callerNumber;
        ImageView callButton;
        MyParentHolder(View itemView) {
            callerNumber = (TextView) itemView.findViewById(R.id.caller_number);
            callButton = (ImageView) itemView.findViewById(R.id.call);
        }
    }

    private class MyChildHolder {

        public TextView play,date;
        ImageView callImage;

        MyChildHolder(View itemView) {
            date = (TextView) itemView.findViewById(R.id.tv_date);
            play = (TextView) itemView.findViewById(R.id.tv_play);
            callImage = (ImageView) itemView.findViewById(R.id.call_img);
        }
    }
    private class MediaHolder{

        TextView date, recEndPoint, recCurrPoint;
        SeekBar seekBar;
        ImageView downloadImage, playButton;

        MediaHolder(View itemView){

            downloadImage = (ImageView) itemView.findViewById(R.id.download);
            date= (TextView) itemView.findViewById(R.id.date);
            playButton = (ImageView) itemView.findViewById(R.id.imgview_play);
            recEndPoint = (TextView) itemView.findViewById(R.id.tv_end_time);
            recCurrPoint = (TextView) itemView.findViewById(R.id.tv_current_time);
            seekBar = (SeekBar) itemView.findViewById(R.id.seekBar);
        }
    }
    private void downloadFile(String fileurl, String filename, final ImageView downloadImage){
        File file = initProfilePicFolder(filename);
        int count = 0;
        try {
            URL url = new URL(fileurl);
            URLConnection conexion = url.openConnection();
            conexion.connect();
            // this will be useful so that you can show a tipical 0-100% progress bar
            int lenghtOfFile = conexion.getContentLength();

            // downlod the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                Log.v("ggg",(int)(total*100/lenghtOfFile)+" ");
                publishHandler(downloadImage,total,lenghtOfFile);
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.v("ggg",count+" "+e.getMessage());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(mContext,R.color.primary), PorterDuff.Mode.SRC_IN);
                    downloadImage.setColorFilter(porterDuffColorFilter);
                    Toast.makeText(mContext, mContext.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show();
                }
            },400);
        }
    }

    private void publishHandler(final ImageView downloadImage, final long total, final int lengthOfFile){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if((int)(total*100/lengthOfFile) == 100){
                    PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(mContext,R.color.primaryColor), PorterDuff.Mode.SRC_IN);
                    downloadImage.setColorFilter(porterDuffColorFilter);
                    Toast.makeText(mContext, mContext.getString(R.string.successfully_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        },500);
    }
    private File initProfilePicFolder(String file) {
        String dateFile = file+"_"+new SimpleDateFormat("dd/MM/yyyy_KK:mm_a", Locale.ENGLISH).format(new Date());
        File ProfilePicFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Boost/");
        if (!ProfilePicFolder.exists()) {
            Log.v("ggg",ProfilePicFolder.mkdirs()+" ");
        }
        File ProfilePicFile;
        for (int i=1;;i++)
        {
            ProfilePicFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Boost/"+dateFile+".mp3");
            if (ProfilePicFile.exists()) {
                dateFile = dateFile.replaceAll("(.*?)","");
                dateFile+="("+i+")";
                Log.v("ggg",dateFile);
            }else
            {
                try {
                    if (ProfilePicFile.createNewFile()) {
                        Log.d("ggg","Successfully created the parent dir:" + ProfilePicFile.getName());
                    } else {
                        Log.d("ggg","Failed to create the parent dir:" + ProfilePicFile.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v("ggg","error");
                }
                break;
            }
        }
        return ProfilePicFile;
    }

    public interface RequestPermission{
        void reuestStoragePermission();
    }
}

