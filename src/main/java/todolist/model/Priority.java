package todolist.model;

public enum Priority {
    HIGH("Alta"), MEDIUM("Media"), LOW("Baja");
    private final String label;
    Priority(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
