public class DiskRequest {
    public int track;       
    public int movement;     
    public boolean served;   

    public DiskRequest(int track) {
        this.track = track;
        this.served = false;
        this.movement = 0;
    }
}
