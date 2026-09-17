package atlas;

import java.util.Scanner;

class InputThread implements Runnable {

    private AtlasState state;

    InputThread(AtlasState state) {
        this.state = state;
    }


    @Override
    public void run() {

        Scanner scanner = new Scanner(System.in);

        while (true) {


            String input = scanner.nextLine();

            System.out.println("\n=======================================");

            try {
            
                state.actionQueue.put(new CommandItem(input));
            
            } catch (InterruptedException e) {
                
                Thread.currentThread().interrupt();
                return;
            
            }


        }

    }
}