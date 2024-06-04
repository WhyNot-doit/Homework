#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define TABLE_SIZE 50
#define MAX_CITIES 20

// ���徰��ṹ��
typedef struct Spot {
    int id;
    char name[50];
    char city[50];
    float recommended_time;
    struct Spot *next;
} Spot;

// ���徰������ṹ��
typedef struct {
    Spot *head;
} SpotList;

// �����ϣ��ṹ��
typedef struct {
    Spot *table[TABLE_SIZE];
} HashTable;

// ����ͼ�ṹ��
typedef struct {
    int distance[MAX_CITIES][MAX_CITIES];
    int numCities;
} Graph;

// ��ϣ����
unsigned int hash(int key) {
    return key % TABLE_SIZE;
}

// ��������
SpotList *createList() {
    SpotList *list = (SpotList *)malloc(sizeof(SpotList));
    list->head = NULL;
    return list;
}

// ��Ӿ�����Ϣ
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

// ��ѯ������Ϣ
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

// ����������Ϣ
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

// ɾ��������Ϣ
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

// �޸ľ�����Ϣ
void modifySpot(SpotList *list, int id, const char *new_name, const char *new_city, float new_recommended_time) {
    Spot *spot = querySpot(list, id);
    if (spot != NULL) {
        strcpy(spot->name, new_name);
        strcpy(spot->city, new_city);
        spot->recommended_time = new_recommended_time;
    }
}

// ������ϣ��
HashTable *createHashTable() {
    HashTable *hashTable = (HashTable *)malloc(sizeof(HashTable));
    for (int i = 0; i < TABLE_SIZE; i++) {
        hashTable->table[i] = NULL;
    }
    return hashTable;
}

// ���뾰����Ϣ����ϣ��
void insertHashTable(HashTable *hashTable, Spot *spot) {
    unsigned int index = hash(spot->id);
    while (hashTable->table[index] != NULL) {
        index = (index + 1) % TABLE_SIZE;
    }
    hashTable->table[index] = spot;
}

// ���Ҿ�����Ϣ
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

// ����ͼ
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

// ��ӱ�
void addEdge(Graph *graph, int city1, int city2, int distance) {
    graph->distance[city1][city2] = distance;
    graph->distance[city2][city1] = distance;
}

// Dijkstra�㷨
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

    printf("���·����\n");
    int path[MAX_CITIES];
    int count = 0, u = endCity;
    while (u != -1) {
        path[count++] = u;
        u = prev[u];
    }
    for (int i = count - 1; i >= 0; i--) {
        printf("%d ", path[i]);
    }
    printf("\n�ܺ�ʱ��%.2f��\n", dist[endCity] * 0.25);
}

int main() {
    SpotList *list = createList();
    HashTable *hashTable = createHashTable();

    addSpot(list, 1, "��¥������ʷ�Ļ�����", "����", 0.5);
    addSpot(list, 2, "���������ɽȺ������ʹ�԰", "����", 1.0);
    insertHashTable(hashTable, list->head);
    insertHashTable(hashTable, list->head->next);

    int choice;
    do {
        printf("\n��ӭʹ�ú����ǻ����η���ϵͳ\n");
        printf("��ѡ�������\n");
        printf("1. ¼�뾰����Ϣ\n");
        printf("2. ��ѯ������Ϣ\n");
        printf("3. ����������Ϣ\n");
        printf("4. ɾ��������Ϣ\n");
        printf("5. �޸ľ�����Ϣ\n");
        printf("6. ���Ҿ�����Ϣ\n");
        printf("7. �滮����·��\n");
        printf("8. �˳�\n");

        scanf("%d", &choice);
        switch (choice) {
            case 1: {
                int id;
                char name[50], city[50];
                float recommended_time;
                printf("���뾰����ţ�");
                scanf("%d", &id);
                printf("���뾰�����ƣ�");
                scanf("%s", name);
                printf("���뾰���������أ�");
                scanf("%s", city);
                printf("�����Ƽ�����ʱ�䣨�죩��");
                scanf("%f", &recommended_time);
                addSpot(list, id, name, city, recommended_time);
                insertHashTable(hashTable, list->head);  // ��ʾ�����������һ������
                printf("������Ϣ¼��ɹ�\n");
                break;
            }
            case 2: {
                int id;
                printf("�����ѯ�ľ�����ţ�");
                scanf("%d", &id);
                Spot *spot = querySpot(list, id);
                if (spot) {
                    printf("������ţ�%d�����ƣ�%s���������أ�%s���Ƽ�����ʱ�䣺%.2f��\n",
                           spot->id, spot->name, spot->city, spot->recommended_time);
                } else {
                    printf("δ�ҵ��þ�����Ϣ\n");
                }
                break;
            }
            case 3: {
                int after_id, id;
                char name[50], city[50];
                float recommended_time;
                printf("�������λ�õľ�����ţ�");
                scanf("%d", &after_id);
                printf("�����¾������ţ�");
                scanf("%d", &id);
                printf("�����¾�������ƣ�");
                scanf("%s", name);
                printf("�����¾�����������أ�");
                scanf("%s", city);
                printf("�����¾�����Ƽ�����ʱ�䣨�죩��");
                scanf("%f", &recommended_time);
                insertSpotAfter(list, after_id, id, name, city, recommended_time);
                printf("����������Ϣ�ɹ�\n");
                break;
            }
            case 4: {
                int id;
                printf("����ɾ���ľ�����ţ�");
                scanf("%d", &id);
                deleteSpot(list, id);
                printf("ɾ��������Ϣ�ɹ�\n");
                break;
            }
            case 5: {
                int id;
                char new_name[50], new_city[50];
                float new_recommended_time;
                printf("�����޸ĵľ�����ţ�");
                scanf("%d", &id);
                printf("���������ƣ�");
                scanf("%s", new_name);
                printf("�������������أ�");
                scanf("%s", new_city);
                printf("�������Ƽ�����ʱ�䣨�죩��");
                scanf("%f", &new_recommended_time);
                modifySpot(list, id, new_name, new_city, new_recommended_time);
                printf("�޸ľ�����Ϣ�ɹ�\n");
                break;
            }
            case 6: {
                int id;
                printf("������ҵľ�����ţ�");
                scanf("%d", &id);
                Spot *spot = findSpot(hashTable, id);
                if (spot) {
                    printf("������ţ�%d�����ƣ�%s���������أ�%s���Ƽ�����ʱ�䣺%.2f��\n",
                           spot->id, spot->name, spot->city, spot->recommended_time);
                } else {
                    printf("δ�ҵ��þ�����Ϣ\n");
                }
                break;
            }
            case 7: {
                Graph *graph = createGraph(MAX_CITIES);
                // ��ʼ�����м���룬�����Լ�ʾ�����趨�������ؾ���Ϊ1
                for (int i = 0; i < MAX_CITIES - 1; i++) {
                    addEdge(graph, i, i + 1, 1);
                }
                dijkstra(graph, 0, MAX_CITIES - 1);  // ����0Ϊ���ڣ�MAX_CITIES-1Ϊ����
                break;
            }
            case 8:
                printf("�˳�ϵͳ\n");
                break;
            default:
                printf("��Ч�Ĳ�����ţ�����������\n");
        }
    } while (choice != 8);

    return 0;
}


