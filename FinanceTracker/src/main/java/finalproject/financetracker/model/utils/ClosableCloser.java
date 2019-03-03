package finalproject.financetracker.model.utils;

import finalproject.financetracker.model.utils.emailing.EmailSender;

public class ClosableCloser extends Thread {
    private AutoCloseable closable;
    private String closableDescription;
    public ClosableCloser(AutoCloseable closable, String closableDescription) {
        this.closable = closable;
        this.closableDescription = closableDescription;
    }

    @Override
    public void run() {
        synchronized (EmailSender.reminderLock) {
            try {
                EmailSender.reminderLock.wait(5000);
            } catch (InterruptedException ex) {
                System.out.println(
                        "Thread at UserDao interrupted while waiting for reminderLock notification:" +
                                ex.getMessage()
                );
            }
        }
        if (this.closable != null) {
            try {
                this.closable.close();
                System.out.println("------- " + this.closableDescription + " closed! ------");
            } catch (Exception ex) {
                System.out.println("Failed to close resource: " + ex.getMessage());
            }
        }
    }
}
