package MapGenerator;
// City Creator
// Also have BSP realization

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Step3 {
    BufferedImage img;
    BufferedImage buildings[];
    String buildingNames[];
    int[] chance;
    int[] ch;
    int[] quantity;
    int[] onMap;

    int[][] map;
    int[][] mapb;
    int size;
    int X, Y;
    int r;

    int min = 70;



    public void setBuildings() throws IOException {
        File list = new File("images\\buildings\\list.txt");
        Scanner sc = new Scanner(list);
        int len = sc.nextInt();
        buildings  = new BufferedImage[len];
        buildingNames  = new String[len];
        quantity = new int[len];
        ch = new int[len];
        chance = new int[len];
        onMap = new int[len];
        for(int i = 0; i < len; i ++){
            sc.nextLine();
            String name = sc.next();
            buildingNames[i] = name;
            buildings[i] = ImageIO.read(new File("images\\buildings\\"+name+".png"));
            quantity[i] = sc.nextInt();
            ch[i] = sc.nextInt();
            chance[i] = sc.nextInt();
        }
    }


    public Step3(int[][] map, BufferedImage img, long time) throws IOException {
        setBuildings();
        this.map = map;
        size = map.length;
        this.img = img;
        Check();
        BSP();
        Write(time);
    }



//BSP
    class quad{
        int maxx = 0, minx = 0;
        int maxy = 0, miny = 0;
        public quad(int x0, int x1, int y0, int y1) {
            maxx = x1;
            minx = x0;
            maxy = y1;
            miny = y0;
        }
        boolean good(){
            return  (maxx - minx > min && maxy - miny > min);
        }

        void dig(){
            maxx--; minx++;
            maxy--; miny++;
            minx++;
            miny++;
            while (maxx-minx >= 30 && maxy-miny >= 30) {
                for(int i = 0; i < buildings.length; i ++) {
                    if (maxx - minx >= buildings[i].getWidth() && maxy - miny >= buildings[i].getHeight()) {
                        if (quantity[i] > 0) {
                            quantity[i]--;
                            putBuilding(minx, miny, i);
                            for (int k = 0; k < chance.length; k++) {
                                if (Math.random() * 500 < chance[k]) {
                                    quantity[k]++;
                                    chance[k] = 0;
                                }
                                chance[k] += ch[k];
                            }
                            dig(minx, minx + buildings[i].getWidth(), miny, miny + buildings[i].getHeight());
                            if (maxx - minx > maxy - miny) minx += buildings[i].getWidth();
                            else miny += buildings[i].getHeight();
                        }
                    }
                }
            }
        }
        void dig(int sx, int fx, int sy, int fy){
            for(int i = sx; i < fx; i ++){
                for(int j = sy; j < fy; j++) {
                    mapb[i][j] = 1;
                }
            }
        }
    }
    public void BSP(){

        mapb = new int[size][size];
        for(int i = 0; i < size; i ++){
            for(int j = 0; j < size; j++) {
                mapb[i][j] = 1;
            }
        }

        tree(new quad(X-r,X+r,Y-r,Y+r));
        for(int i = 0; i < size; i ++){
            for(int j = 0; j < size; j++) {
                img.setRGB(i,j, (img.getRGB(i,j)*(mapb[i][j])));
            }
        }
    }
    void tree(quad parent){
        double d = Math.random();
        boolean r;
        quad one, two;
        if(Math.random() > 0.5) {
            r = true;
            d = Math.min(Math.max((d*(parent.maxy-parent.miny)), 34), parent.maxy-parent.miny - 34)+parent.miny;
            one = new quad(parent.minx, parent.maxx, parent.miny, (int)d);
            two = new quad(parent.minx, parent.maxx, (int)d, parent.maxy);
        } else {
            r = false;
            d = Math.min(Math.max((d*(parent.maxx-parent.minx)), 34), parent.maxx-parent.minx - 34)+parent.minx;
            one = new quad(parent.minx, (int)d, parent.miny, parent.maxy);
            two = new quad((int)d, parent.maxx, parent.miny, parent.maxy);
        }
        if(one.good()) tree(one); else one.dig();
        if(two.good()) tree(two); else two.dig();
//        makeway(one, two, (int)d, r);
    }
//    private void makeway(quad one, quad two, int d, boolean r) {
//        int mx, mn;
//        if(r){
//            mx = Math.min(one.maxx,two.maxx);
//            mn = Math.max(one.minx,two.minx);
//        } else {
//            mx = Math.min(one.maxy,two.maxy);
//            mn = Math.max(one.miny,two.miny);
//        }
//        int j = d;
//        int i = mn;
//        while (dig(i,j,r) && i <= mx) i++;
//    }
//
//    private boolean dig(int i, int j, boolean r){
//        if(i+1 >=size || j+1>= size || i-1<0 || j-1 < 0) return false;
//        if(r) {
//            if(mapb[i][j] != 1) return false;
//            mapb[i][j] = 2;
//            mapb[i][j+1] = 2;
//            mapb[i][j-1] = 2;
//        } else {
//            if(mapb[j][i] != 1) return false;
//            mapb[j][i] = 2;
//            mapb[j+1][i] = 2;
//            mapb[j-1][i] = 2;
//        }
//        return true;
//    }





//place finder
    void Check(){
        r = size*2/5;
        boolean b = true;
        while(true) {
            r -= size/30;
            for (int i = r; i < size - r; i++) {
                for (int j = r; j < size - r; j++) {
                    if (map[i][j] != 0) continue;
                    b = true;
                    for (int x = 0; x < r; x++) {
                        int y = (int)Math.sqrt(r*r - x*x);
                        if (map[i+x][j+y] != 0) b = false;
                        if (map[i+x][j-y] != 0) b = false;
                        if (map[i-x][j+y] != 0) b = false;
                        if (map[i-x][j-y] != 0) b = false;
                    }
                    if(b) {
                        X = i;
                        Y = j;
                        return;
                    }
                }
            }
        }
    }




    void putBuilding(int x, int y, int k){
        for(int i = 0; i < buildings[k].getWidth(); i ++){
            for(int j = 0; j < buildings[k].getHeight(); j ++) {
                img.setRGB(x+i, y+j, buildings[k].getRGB(i,j));
            }
        }
        onMap[k]++;
    }

    void Write(long time) throws IOException {
        File file = new File("Results\\"+time+"\\locations.dnd");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        for (int i = 0; i < buildings.length; i ++) {
            writer.append(buildingNames[i] + ":" + onMap[i]+"\n");
        }
        writer.close();
    }


    BufferedImage getImg(){
        return img;
    }

}
