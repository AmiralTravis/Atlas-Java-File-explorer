package atlas;

import java.util.Scanner;

class InputThread implements Runnable {

    private AtlasState state;

    InputThread(AtlasState state) {
        this.state = state;
    }


    @Override
    public void run() {

        try (Scanner scanner = new Scanner(System.in)) {

            while (true) {


                String input = scanner.nextLine();

                try {

                    if (!input.equals("")) {

                        state.actionQueue.put(new CommandItem(input));

                        System.out.println("\n=======================================\n");
                    
                    } else {
                    
                        System.out.print("\033[1A"); // Move cursor up one line
                        System.out.print("\033[2K"); // Clear that line

                        continue;
                    
                    }
                
                } catch (InterruptedException e) {
                    
                    Thread.currentThread().interrupt();
                    return;
                
                }


            }
        
        }

        

    }
}