package np.com.naxa.staffattendance;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import np.com.naxa.staffattendance.attendence.TeamMemberResposne;
import np.com.naxa.staffattendance.utlils.FlipAnimator;
import timber.log.Timber;

public class StaffListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<TeamMemberResposne> filetredsitelist;
    private List<TeamMemberResposne> staffList;
    private List<String> attedanceIds;
    private OnStaffItemClickListener listener;
    private SparseBooleanArray selectedItems;
    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;
    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;
    private boolean enablePersonSelection;
    private HashMap<Integer, String> selectedStaffHashMap = new HashMap<>();


    public StaffListAdapter(Context mContext, List<TeamMemberResposne> staffList, boolean enablePersonSelection, List<String> attedanceIds, OnStaffItemClickListener listener) {
        this.mContext = mContext;
        this.staffList = staffList;
        this.filetredsitelist = staffList;
        this.attedanceIds = attedanceIds;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
        this.enablePersonSelection = enablePersonSelection;

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.staff_list_row, parent, false);

        return new StaffVH(itemView);
    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, int position) {
        final TeamMemberResposne staff = staffList.get(holder.getAdapterPosition());
        final StaffVH staffVH = (StaffVH) holder;
        setupPreviousAttendance(attedanceIds, staff.getId(), staffVH);

        staffVH.rootLayout.setEnabled(enablePersonSelection);
        staffVH.staffName.setText(staff.getFirstName());
        staffVH.staffPost.setText(staff.getDesignationLabel());

        staffVH.iconText.setVisibility(View.VISIBLE);
        staffVH.imgProfile.setImageResource(R.drawable.circle_blue);
        staffVH.iconText.setText(staff.getFirstName().substring(0, 1));

        staffVH.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onStaffClick(staffVH.getAdapterPosition(), staff);
            }
        });

        staffVH.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onStaffLongClick(staffVH.getAdapterPosition());
                return true;
            }
        });
    }

    private void setupPreviousAttendance(List<String> attedanceIds, String staffId, StaffVH staffVH) {
        boolean isPresentOnThisDay = contains(attedanceIds, staffId);

        if (isPresentOnThisDay) {
            selectedItems.put(staffVH.getAdapterPosition(), true);
            animationItemsIndex.put(staffVH.getAdapterPosition(), true);
            applyAnimToPastAttedanceItems(staffVH, staffVH.getAdapterPosition());
        } else {
            applyAnimToTodayAttedanceItems(staffVH, staffVH.getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public void markPresent(String staffId) {
        for (int i = 0; i < staffList.size(); i++) {
            if (staffList.get(i).getId().equals(staffId)) {
                Timber.i("Found staff %s / %s", staffId, i);
                toggleSelection(i);
                return;
            }
        }
        Timber.i("Not Found staff: %s", staffId);
    }


    public void notifyChanges(List<String> newAttendanceIds) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return attedanceIds.size();
            }

            @Override
            public int getNewListSize() {
                return newAttendanceIds.size();
            }

            @Override
            public boolean areItemsTheSame(int i, int i1) {
                return attedanceIds.get(i).equals(attedanceIds.get(i1));
            }

            @Override
            public boolean areContentsTheSame(int i, int i1) {
                return attedanceIds.get(i).equals(attedanceIds.get(i1));
            }
        });

        diff.dispatchUpdatesTo(this);
    }


    public void toggleSelection(int pos) {

        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);

            if (selectedStaffHashMap.containsKey(pos)) {
                selectedStaffHashMap.remove(pos);
            }

        } else {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
            selectedStaffHashMap.put(pos, staffList.get(pos).getId());
        }

        notifyItemChanged(pos);
    }


    public ArrayList<String> getSelectedStaffIds() {
        ArrayList<String> staffIds = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : selectedStaffHashMap.entrySet()) {
            staffIds.add(entry.getValue());
        }

        return staffIds;
    }

    private void applyAnimToTodayAttedanceItems(StaffVH holder, int position) {
        applyIconAnimation(holder, position, true);
    }

    private void applyAnimToPastAttedanceItems(StaffVH holder, int position) {
        applyIconAnimation(holder, position, false);
    }

    private void applyIconAnimation(StaffVH holder, int position, boolean shouldHightlight) {
        if (selectedItems.get(position, false)) {

            int colorGreen = ContextCompat.getColor(holder.rootLayout.getContext(), R.color.green);
            holder.staffStatus.setTextColor(colorGreen);

            holder.rootLayout.setActivated(shouldHightlight);
            holder.staffStatus.setText(holder.rootLayout.getContext().getString(R.string.attedance_present));


            holder.iconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE);
            holder.iconBack.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, true);
                resetCurrentIndex();
            }
        } else {
            int colorRed = ContextCompat.getColor(holder.rootLayout.getContext(), R.color.red);
            holder.staffStatus.setTextColor(colorRed);

            holder.staffStatus.setText(holder.rootLayout.getContext().getString(R.string.attedance_absent));


            holder.rootLayout.setActivated(!shouldHightlight);
            holder.iconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.iconFront);
            holder.iconFront.setVisibility(View.VISIBLE);
            holder.iconFront.setAlpha(1);
            if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false);
                resetCurrentIndex();
            }
        }
    }

    // As the views will be reused, sometimes the icon appears as
    // flipped because older view is reused. Reset the Y-axis to 0
    private void resetIconYAxis(View view) {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    public void resetAnimationIndex() {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }


    public ArrayList<TeamMemberResposne> getSelected() {
        ArrayList<TeamMemberResposne> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(staffList.get(i));
        }

        return items;
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public ArrayList<String> getSelectedStaffID() {
        ArrayList<String> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(staffList.get(i).getId());
        }

        return items;
    }


    public class StaffVH extends RecyclerView.ViewHolder {
        private TextView staffName, staffStatus, siteAddress, sitePhone, staffType, sitePendingFormsNumber, site, iconText, timestamp, tvTagOfflineSite, staffPost;
        private ImageView iconImp, imgProfile;
        private RelativeLayout iconContainer, iconBack, iconFront;
        private RelativeLayout rootLayout;
        private CardView card;


        public StaffVH(View view) {
            super(view);
            card = view.findViewById(R.id.card_site_lst_row);
            iconBack = view.findViewById(R.id.icon_back);
            iconFront = view.findViewById(R.id.icon_front);
            iconContainer = view.findViewById(R.id.icon_container);
            rootLayout = view.findViewById(R.id.root_layout);
            staffStatus = view.findViewById(R.id.staff_list_row_status);

            staffName = view.findViewById(R.id.staff_list_row_name);
            staffPost = view.findViewById(R.id.staff_list_row_post);
            siteAddress = view.findViewById(R.id.staff_list_row_email);
            sitePhone = view.findViewById(R.id.staff_list_row_phone);
            staffType = view.findViewById(R.id.staff_list_row_type);
            iconText = view.findViewById(R.id.icon_text);
            imgProfile = view.findViewById(R.id.icon_profile);
        }
    }

    public interface OnStaffItemClickListener {
        void onStaffClick(int pos, TeamMemberResposne staff);

        void onStaffLongClick(int pos);
    }

    private boolean contains(List<String> list, String comparable) {
        //todo why did list.containts(string) not work?
        if (list == null || list.size() <= 0) {
            return false;
        }

        for (String string : list) {
            if (string.trim().equalsIgnoreCase(comparable)) {
                return true;
            }
        }
        return false;
    }
}

