#include<iostream>

using namespace std;
const int N = 1e5 + 1;
int a[N];
int book[2];//用0和1来存位置
void search(int l,int r, int key)
{
    int mid = (l+r) >> 1;
    if(l>r) 
    {
        book[0] = -1;
        book[1] = -1;
        return;
    }
    if(a[mid] == key) 
    {   if(a[mid-1] == key) book[0] = mid - 1;
        else book[0] = mid;
        int i = mid;
        while(a[i] == mid) i++;
        book[1] = i;
        return;
    }
    else if(a[mid] < key) search(mid+1,r,key);
    else search(l,mid,key);
}
int main()
{
    
    int n,q;
    cin >> n >>q;
    for(int i = 0; i < n ;i++) cin >> a[i];  
    while(q--)
    {
        int key;
        cin >> key;
        search(0,n-1,key);
        cout<<book[0]<<" "<<book[1]<<"\n";
    }
    return 0;
}
