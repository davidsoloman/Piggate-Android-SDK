/**
 *
 *  Copyright 2015-present Piggate
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package samples.piggate.com.piggateInfoDemo;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.melnykov.fab.FloatingActionButton;
import com.piggate.sdk.PiggateInfo;

import java.util.ArrayList;
/*
Recyclerview adapter for the info content list in Activity_Logged
*/
public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {

    private static ArrayList<PiggateInfo> infoList; //Offer list
    static Context mContext; //Context of the application

    //Public constructor
    public InfoAdapter(ArrayList<PiggateInfo> myInfoList, Context context){
        infoList = myInfoList;
        mContext = context;
    }

    //Initialize the ViewHolder
    @Override
    public InfoAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.info_card, viewGroup, false);
        ViewHolder vh = new ViewHolder((v));
        return vh;
    }

    //Set the fields for every offer
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        viewHolder.infoTitle.setText(infoList.get(i).getName().toString());
        viewHolder.infoDescription.setText(infoList.get(i).getDescription().toString().substring(0,75)+"...");
        viewHolder.infoImage.setImageUrl(infoList.get(i).get_imgURL().toString());
    }

    //Offer list size
    @Override
    public int getItemCount() {
        return infoList.size();
    }

    //Add info element to the list
    public void add(PiggateInfo item, int position) {
        infoList.add(position, item);
        notifyItemInserted(position);
    }

    //Remove info element from the list
    public void remove(PiggateInfo item) {
        int position = infoList.indexOf(item);
        infoList.remove(position);
        notifyItemRemoved(position);
    }

    //ViewHolder for performance parameters
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView infoTitle;
        public TextView infoDescription;
        public SmartImageView infoImage;
        public FloatingActionButton seeMoreButton;

        public ViewHolder(View itemView) {
            super(itemView);

            //Initialize the elements of the offer
            infoTitle = (TextView) itemView.findViewById(R.id.InfoTitle1);
            infoDescription= (TextView)itemView.findViewById(R.id.InfoDescription1);
            seeMoreButton = (FloatingActionButton)itemView.findViewById(R.id.seeMoreButton);
            infoImage = (SmartImageView) itemView.findViewById(R.id.infoImage1);

            //onClick listener for the buy button (start the buyOffer Activity)
            seeMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent slideactivity = new Intent(mContext, InfoActivity.class);
                    slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    //Information about selected content
                    slideactivity.putExtra("infoTitle", infoList.get(getPosition()).getName().toString());
                    slideactivity.putExtra("infoDescription", infoList.get(getPosition()).getDescription().toString());
                    slideactivity.putExtra("infoImageURL", infoList.get(getPosition()).get_imgURL().toString());
                    slideactivity.putExtra("infoVideoURL", infoList.get(getPosition()).get_videoURL().toString());

                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(mContext, R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                    mContext.startActivity(slideactivity, bndlanimation);
                }
            });
        }

        @Override
        public void onClick(View view){
            //OnClick for the entire CardView element
        }
    }
}
