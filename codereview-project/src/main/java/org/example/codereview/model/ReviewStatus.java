package org.example.codereview.model;

public enum ReviewStatus {
    WIP("Черновик"),
    ACTIVE("На ревью"),
    CHANGES_REQUIRED("Требуются правки"),
    APPROVED("Одобрено"),
    CLOSED("Замержено"),
    ABANDONED("Отменено");

    private final String label;

    ReviewStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTerminal() {
        return this == CLOSED || this == ABANDONED;
    }

    public boolean canTransitionTo(ReviewStatus target) {
        if (target == null || target == this) {
            return false;
        }

        switch (this) {
            case WIP:
                return target == ACTIVE
                        || target == ABANDONED;
            case ACTIVE:
                return target == CHANGES_REQUIRED
                        || target == CHANGES_REQUIRED
                        || target == APPROVED
                        || target == ABANDONED;
            case CHANGES_REQUIRED:
                return target == ACTIVE
                        || target == ABANDONED;
            case APPROVED:
                return target == CHANGES_REQUIRED
                        || target == CLOSED
                        || target == ABANDONED;
            case CLOSED:
            case ABANDONED:
            default:
                return false;
        }
    }

    public static ReviewStatus fromDatabase(String raw) {
        if (raw == null || raw.isBlank()) {
            return WIP;
        }

        String normalized = raw.trim().toUpperCase();
        try {
            if ("OPEN".equals(normalized)) {
                return WIP;
            }
            if ("IN_REVIEW".equals(normalized)) {
                return ACTIVE;
            }
            return ReviewStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return WIP;
        }
    }
}

