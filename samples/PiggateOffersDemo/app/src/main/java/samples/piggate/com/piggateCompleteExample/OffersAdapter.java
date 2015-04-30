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

package samples.piggate.com.piggateCompleteExample;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.melnykov.fab.FloatingActionButton;
import com.piggate.sdk.PiggateOffers;

import java.util.ArrayList;
/*
Recyclerview adapter for the offer list in Activity_Logged
*/
public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.ViewHolder> {

        private static ArrayList<PiggateOffers> offers; //Offer list
        static Context mContext; //Context of the application

        //Public constructor
        public OffersAdapter(ArrayList<PiggateOffers> myOffers, Context context){
            offers = myOffers;
            mContext = context;
        }

        //Initialize the ViewHolder
        @Override
        public OffersAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card, viewGroup, false);
            ViewHolder vh = new ViewHolder((v));
            return vh;
        }

        //Set the fields for every offer
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {

            viewHolder.offerTitle.setText(offers.get(i).getName().toString());
            viewHolder.offerText.setText(offers.get(i).getDescription().toString());
            viewHolder.offerPrice.setText(offers.get(i).getPrice().toString() + " " + offers.get(i).getCurrency().toString());
            viewHolder.image.setImageUrl(offers.get(i).getImgURL().toString());
        }

        //Offer list size
        @Override
        public int getItemCount() {
            return offers.size();
        }

        //Add offers to the list
        public void add(PiggateOffers item, int position) {
            offers.add(position, item);
            notifyItemInserted(position);
        }

        //Remove offers from the list
        public void remove(PiggateOffers item) {
            int position = offers.indexOf(item);
            offers.remove(position);
            notifyItemRemoved(position);
        }

        //ViewHolder for performance parameters
        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            public TextView offerTitle;
            public TextView offerText;
            public Button offerPrice;
            public SmartImageView image;
            public FloatingActionButton mapsButton;

            public ViewHolder(View itemView) {
                super(itemView);

                //Initialize the elements of the offer
                offerTitle = (TextView) itemView.findViewById(R.id.offerTitle);
                offerText= (TextView)itemView.findViewById(R.id.offerText);
                offerPrice = (Button)itemView.findViewById(R.id.offerPrice);
                mapsButton = (FloatingActionButton) itemView.findViewById(R.id.mapsButton);
                image = (SmartImageView) itemView.findViewById(R.id.offerImage);

                //onClick listener for the buy button (start the buyOffer Activity)
                offerPrice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent slideactivity = new Intent(mContext, buyOfferActivity.class);
                        slideactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        //Information about selected offer
                        slideactivity.putExtra("offerName", offers.get(getPosition()).getName().toString());
                        slideactivity.putExtra("offerDescription", offers.get(getPosition()).getDescription().toString());
                        slideactivity.putExtra("offerImgURL", offers.get(getPosition()).getImgURL().toString());
                        slideactivity.putExtra("offerCurrency", offers.get(getPosition()).getCurrency().toString());
                        slideactivity.putExtra("offerID", offers.get(getPosition()).getID().toString());
                        slideactivity.putExtra("offerPrice", offers.get(getPosition()).getPrice().toString());

                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(mContext, R.anim.slidefromright, R.anim.slidetoleft).toBundle();
                        mContext.startActivity(slideactivity, bndlanimation);
                    }
                });

                //OnClick listener for the Google Maps button (starts Google Maps to show the store location)
                mapsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double latitude = offers.get(getPosition()).getLatitude();
                        double longitude = offers.get(getPosition()).getLongitude();
                        String coordinates = String.format("geo:0,0?q=" + latitude + "," + longitude);
                        Intent intentMap = new Intent();
                        intentMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentMap.setAction(Intent.ACTION_VIEW);
                        intentMap.setData(Uri.parse(coordinates));
                        mContext.startActivity(intentMap);
                    }
                });
            }

            @Override
            public void onClick(View view){
                //OnClick for the entire CardView
            }
        }
}