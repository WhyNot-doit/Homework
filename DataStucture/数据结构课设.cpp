#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define TABLE_SIZE 50
#define MAX_CITIES 20

// 定义景点结构体
typedef struct Spot {
    int id;
    char name[50];
    char city[50];
    float recommended_time;
    struct Spot *next;
} Spot;

// 定义景点链表结构体
typedef struct {
    Spot *head;
} SpotList;

// 定义哈希表结构体
typedef struct {
    Spot *table[TABLE_SIZE];
} HashTable;

// 定义图结构体
typedef struct {
    int distance[MAX_CITIES][MAX_CITIES];
    int numCities;
} Graph;

// 哈希函数
unsigned int hash(int key) {
    return key % TABLE_SIZE;
}

// 创建链表
SpotList *createList() {
    SpotList *list = (SpotList *)malloc(sizeof(SpotList));
    list->head = NULL;
    return list;
}

// 添加景点信息
void addSpot(SpotList *list, int id, const char *name, const char *city, float recommended_time) {
    Spot *newSpot = (Spot *)malloc(sizeof(Spot));
    newSpot->id = id;
    strcpy(newSpot->name, name);
    strcpy(newSpot->city, city);
    newSpot->recommended_time = recommended_time;
    newSpot->next = NULL;

    if (list->head == NULL) {
        list->head = newSpot;
    } else {
        Spot *current = list->head;
        while (current->next != NULL) {
            current = current->next;
        }
        current->next = newSpot;
    }
}

// 查询景点信息
Spot *querySpot(SpotList *list, int id) {
    Spot *current = list->head;
    while (current != NULL) {
        if (current->id == id) {
            return current;
        }
        current = current->next;
    }
    return NULL;
}

// 新增景点信息
void insertSpotAfter(SpotList *list, int after_id, int id, const char *name, const char *city, float recommended_time) {
    Spot *current = list->head;
    while (current != NULL && current->id != after_id) {
        current = current->next;
    }
    if (current != NULL) {
        Spot *newSpot = (Spot *)malloc(sizeof(Spot));
        newSpot->id = id;
        strcpy(newSpot->name, name);
        strcpy(newSpot->city, city);
        newSpot->recommended_time = recommended_time;
        newSpot->next = current->next;
        current->next = newSpot;
    }
}

// 删除景点信息
void deleteSpot(SpotList *list, int id) {
    Spot *current = list->head;
    Spot *previous = NULL;
    while (current != NULL && current->id != id) {
        previous = current;
        current = current->next;
    }
    if (current != NULL) {
        if (previous == NULL) {
            list->head = current->next;
        } else {
            previous->next = current->next;
        }
        free(current);
    }
}

// 修改景点信息
void modifySpot(SpotList *list, int id, const char *new_name, const char *new_city, float new_recommended_time) {
    Spot *spot = querySpot(list, id);
    if (spot != NULL) {
        strcpy(spot->name, new_name);
        strcpy(spot->city, new_city);
        spot->recommended_time = new_recommended_time;
    }
}

// 创建哈希表
HashTable *createHashTable() {
    HashTable *hashTable = (HashTable *)malloc(sizeof(HashTable));
    for (int i = 0; i < TABLE_SIZE; i++) {
        hashTable->table[i] = NULL;
    }
    return hashTable;
}

// 插入景点信息到哈希表
void insertHashTable(HashTable *hashTable, Spot *spot) {
    unsigned int index = hash(spot->id);
    while (hashTable->table[index] != NULL) {
        index = (index + 1) % TABLE_SIZE;
    }
    hashTable->table[index] = spot;
}

// 查找景点信息
Spot *findSpot(HashTable *hashTable, int id) {
    unsigned int index = hash(id);
    while (hashTable->table[index] != NULL) {
        if (hashTable->table[index]->id == id) {
            return hashTable->table[index];
        }
        index = (index + 1) % TABLE_SIZE;
    }
    return NULL;
}

// 创建图
Graph *createGraph(int numCities) {
    Graph *graph = (Graph *)malloc(sizeof(Graph));
    graph->numCities = numCities;
    for (int i = 0; i < numCities; i++) {
        for (int j = 0; j < numCities; j++) {
            graph->distance[i][j] = (i == j) ? 0 : 9999;
        }
    }
    return graph;
}

// 添加边
void addEdge(Graph *graph, int city1, int city2, int distance) {
    graph->distance[city1][city2] = distance;
    graph->distance[city2][city1] = distance;
}

// Dijkstra算法
void dijkstra(Graph *graph, int startCity, int endCity) {
    int dist[MAX_CITIES];
    int visited[MAX_CITIES] = {0};
    int prev[MAX_CITIES];

    for (int i = 0; i < graph->numCities; i++) {
        dist[i] = 9999;
        prev[i] = -1;
    }
    dist[startCity] = 0;

    for (int i = 0; i < graph->numCities - 1; i++) {
        int min = 9999, u = -1;
        for (int j = 0; j < graph->numCities; j++) {
            if (!visited[j] && dist[j] <= min) {
                min = dist[j];
                u = j;
            }
        }
        if (u == -1) break;
        visited[u] = 1;

        for (int v = 0; v < graph->numCities; v++) {
            if (!visited[v] && graph->distance[u][v] && dist[u] + graph->distance[u][v] < dist[v]) {
                dist[v] = dist[u] + graph->distance[u][v];
                prev[v] = u;
            }
        }
    }

    printf("最短路径：\n");
    int path[MAX_CITIES];
    int count = 0, u = endCity;
    while (u != -1) {
        path[count++] = u;
        u = prev[u];
    }
    for (int i = count - 1; i >= 0; i--) {
        printf("%d ", path[i]);
    }
    printf("\n总耗时：%.2f天\n", dist[endCity] * 0.25);
}

int main() {
    SpotList *list = createList();
    HashTable *hashTable = createHashTable();

    addSpot(list, 1, "骑楼建筑历史文化街区", "海口", 0.5);
    addSpot(list, 2, "海南雷琼火山群世界地质公园", "海口", 1.0);
    insertHashTable(hashTable, list->head);
    insertHashTable(hashTable, list->head->next);

    int choice;
    do {
        printf("\n欢迎使用海南智慧旅游服务系统\n");
        printf("请选择操作：\n");
        printf("1. 录入景点信息\n");
        printf("2. 查询景点信息\n");
        printf("3. 新增景点信息\n");
        printf("4. 删除景点信息\n");
        printf("5. 修改景点信息\n");
        printf("6. 查找景点信息\n");
        printf("7. 规划旅游路线\n");
        printf("8. 退出\n");

        scanf("%d", &choice);
        switch (choice) {
            case 1: {
                int id;
                char name[50], city[50];
                float recommended_time;
                printf("输入景点序号：");
                scanf("%d", &id);
                printf("输入景点名称：");
                scanf("%s", name);
                printf("输入景点所在市县：");
                scanf("%s", city);
                printf("输入推荐游玩时间（天）：");
                scanf("%f", &recommended_time);
                addSpot(list, id, name, city, recommended_time);
                insertHashTable(hashTable, list->head);  // 简单示例，仅插入第一个景点
                printf("景点信息录入成功\n");
                break;
            }
            case 2: {
                int id;
                printf("输入查询的景点序号：");
                scanf("%d", &id);
                Spot *spot = querySpot(list, id);
                if (spot) {
                    printf("景点序号：%d，名称：%s，所在市县：%s，推荐游玩时间：%.2f天\n",
                           spot->id, spot->name, spot->city, spot->recommended_time);
                } else {
                    printf("未找到该景点信息\n");
                }
                break;
            }
            case 3: {
                int after_id, id;
                char name[50], city[50];
                float recommended_time;
                printf("输入插入位置的景点序号：");
                scanf("%d", &after_id);
                printf("输入新景点的序号：");
                scanf("%d", &id);
                printf("输入新景点的名称：");
                scanf("%s", name);
                printf("输入新景点的所在市县：");
                scanf("%s", city);
                printf("输入新景点的推荐游玩时间（天）：");
                scanf("%f", &recommended_time);
                insertSpotAfter(list, after_id, id, name, city, recommended_time);
                printf("新增景点信息成功\n");
                break;
            }
            case 4: {
                int id;
                printf("输入删除的景点序号：");
                scanf("%d", &id);
                deleteSpot(list, id);
                printf("删除景点信息成功\n");
                break;
            }
            case 5: {
                int id;
                char new_name[50], new_city[50];
                float new_recommended_time;
                printf("输入修改的景点序号：");
                scanf("%d", &id);
                printf("输入新名称：");
                scanf("%s", new_name);
                printf("输入新所在市县：");
                scanf("%s", new_city);
                printf("输入新推荐游玩时间（天）：");
                scanf("%f", &new_recommended_time);
                modifySpot(list, id, new_name, new_city, new_recommended_time);
                printf("修改景点信息成功\n");
                break;
            }
            case 6: {
                int id;
                printf("输入查找的景点序号：");
                scanf("%d", &id);
                Spot *spot = findSpot(hashTable, id);
                if (spot) {
                    printf("景点序号：%d，名称：%s，所在市县：%s，推荐游玩时间：%.2f天\n",
                           spot->id, spot->name, spot->city, spot->recommended_time);
                } else {
                    printf("未找到该景点信息\n");
                }
                break;
            }
            case 7: {
                Graph *graph = createGraph(MAX_CITIES);
                // 初始化城市间距离，这里以简单示例，设定相邻市县距离为1
                for (int i = 0; i < MAX_CITIES - 1; i++) {
                    addEdge(graph, i, i + 1, 1);
                }
                dijkstra(graph, 0, MAX_CITIES - 1);  // 假设0为海口，MAX_CITIES-1为三亚
                break;
            }
            case 8:
                printf("退出系统\n");
                break;
            default:
                printf("无效的操作编号，请重新输入\n");
        }
    } while (choice != 8);

    return 0;
}


