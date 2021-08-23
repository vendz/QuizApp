package cf.vandit.quizapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder> {
    private List<QuizListModel> quizListModels;
    private OnQuizListItemClicked onQuizListItemClicked;

    public QuizListAdapter(OnQuizListItemClicked onQuizListItemClicked) {
        this.onQuizListItemClicked = onQuizListItemClicked;
    }

    @NonNull
    @Override
    public QuizListAdapter.QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizListAdapter.QuizViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.title.setText(quizListModels.get(position).getName());

        holder.level.setText(quizListModels.get(position).getLevel());

        // assigning imageURL to a string
        String imageUrl = quizListModels.get(position).getImage();

        // loading image with 'Glide' dependency
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(holder.coverImage);

        // Configuration for 'read more' button
        final Boolean[] expandable = {true};
        String listdesc = quizListModels.get(position).getDescription();
        if (listdesc.length() > 150){
            listdesc = listdesc.substring(0, 150);
            holder.showMore.setVisibility(View.VISIBLE);
            holder.description.setText(listdesc + "...");
        }
        else {
            holder.showMore.setText(quizListModels.get(position).getDescription());
        }

        String finalListdesc = listdesc;
        holder.showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.showMore.getText().toString().equalsIgnoreCase("Show More")){
                    holder.description.setMaxLines(Integer.MAX_VALUE);
                    holder.showMore.setText("Show Less");
                    holder.description.setText(quizListModels.get(position).getDescription());
                }
                else {
                    holder.description.setText(finalListdesc + "...");
                    holder.showMore.setText("Show More");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(quizListModels == null) {
            return 0;
        } else {
            return quizListModels.size();
        }
    }

    public void setQuizListModels(List<QuizListModel> quizListModels) {
        this.quizListModels = quizListModels;
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView coverImage;
        private final TextView title;
        private final TextView description;
        private final TextView level;
        private final Button viewQuiz;
        private final Button showMore;
        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            coverImage = itemView.findViewById(R.id.list_image);
            title = itemView.findViewById(R.id.list_title);
            description = itemView.findViewById(R.id.list_desc);
            level = itemView.findViewById(R.id.list_difficulty);
            viewQuiz = itemView.findViewById(R.id.list_btn);
            showMore = itemView.findViewById(R.id.list_read_more);

            viewQuiz.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onQuizListItemClicked.onItemClicked(getAdapterPosition());
        }
    }

    public interface OnQuizListItemClicked {
        void onItemClicked(int position);
    }
}
