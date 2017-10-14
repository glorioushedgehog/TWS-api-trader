package cloud;

import com.ib.controller.NewContract;

class OldContract {
    private int pos;
    private NewContract con;

    int getPos() {
        return pos;
    }

    NewContract getCon() {
        return con;
    }

    OldContract(NewContract cont, int posi) {
        this.pos = posi;
        this.con = cont;
    }
}
