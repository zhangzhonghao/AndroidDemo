package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class OneStrokeActivity extends AppCompatActivity {
    private static final int NODES = 9;
    private List<List<Integer>> edges = new ArrayList<>();
    private List<int[]> nodePositions = new ArrayList<>();
    private boolean[] visited = new boolean[NODES];
    private int[][] adjacency = {
        {0,1,0,1,1,0,0,0,0},
        {1,0,1,0,1,1,0,0,0},
        {0,1,0,0,1,0,1,0,0},
        {1,0,0,0,1,0,0,1,0},
        {1,1,1,1,0,1,1,1,1},
        {0,1,0,0,1,0,0,0,1},
        {0,0,1,0,1,0,0,1,0},
        {0,0,0,1,1,0,1,0,1},
        {0,0,0,0,1,1,0,1,0}
    };
    private int pathLength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_stroke);

        TextView tvStatus = findViewById(R.id.tv_status);
        tvStatus.setText("请从任意节点开始，点击节点形成一笔画");

        findViewById(R.id.btn_new_game).setOnClickListener(v -> resetGame());

        initNodes();
    }

    private void initNodes() {
        GridLayout gridLayout = findViewById(R.id.nodes_container);
        gridLayout.removeAllViews();

        nodePositions.clear();

        for (int i = 0; i < NODES; i++) {
            nodePositions.add(new int[]{(i % 3) * 100 + 50, (i / 3) * 100 + 50});
        }

        for (int i = 0; i < NODES; i++) {
            Button btn = new Button(this);
            btn.setText(String.valueOf(i + 1));
            btn.setTag(i);
            btn.setOnClickListener(v -> onNodeClick((int) v.getTag()));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 100;
            params.height = 100;
            params.rowSpec = GridLayout.spec(i / 3);
            params.columnSpec = GridLayout.spec(i % 3);
            btn.setLayoutParams(params);

            gridLayout.addView(btn);
        }
    }

    private void onNodeClick(int node) {
        if (pathLength == 0) {
            visited[node] = true;
            pathLength = 1;
            highlightNode(node);
        } else {
            int lastNode = -1;
            for (int i = 0; i < NODES; i++) {
                if (visited[i]) {
                    lastNode = i;
                }
            }

            if (adjacency[lastNode][node] == 1 && !visited[node]) {
                visited[node] = true;
                pathLength++;
                highlightNode(node);
                checkWin();
            }
        }
    }

    private void highlightNode(int node) {
        GridLayout gridLayout = findViewById(R.id.nodes_container);
        Button btn = (Button) gridLayout.getChildAt(node);
        btn.setBackgroundColor(0xFFFF9800);
    }

    private void checkWin() {
        int visitedCount = 0;
        for (boolean v : visited) {
            if (v) visitedCount++;
        }

        if (visitedCount == NODES) {
            Toast.makeText(this, "恭喜通关! 完成了一笔画!", Toast.LENGTH_LONG).show();
        }

        TextView tvStatus = findViewById(R.id.tv_status);
        tvStatus.setText("已访问节点数: " + visitedCount + " / " + NODES);
    }

    private void resetGame() {
        visited = new boolean[NODES];
        pathLength = 0;
        initNodes();
        TextView tvStatus = findViewById(R.id.tv_status);
        tvStatus.setText("请从任意节点开始，点击节点形成一笔画");
    }
}