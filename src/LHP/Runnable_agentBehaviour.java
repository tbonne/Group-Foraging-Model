package LHP;

public class Runnable_agentBehaviour implements Runnable {


	Primate primate;

	Runnable_agentBehaviour(Primate p){
		primate = p;
	}

	@Override
	public void run(){
		Throwable thrown = null;
		try {
			//System.out.println("running behaviour");
			primate.behaviouralResponse();
		} catch (Throwable e) {
			thrown = e;
			//System.out.println("Problem lies in behaviour code" + thrown);
		} finally {
			return;
		}
	}


}
