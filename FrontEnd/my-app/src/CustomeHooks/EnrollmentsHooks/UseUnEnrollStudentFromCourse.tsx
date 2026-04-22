import { useMutation } from "@tanstack/react-query";
import { unenrollStudentFromCourse } from "../../Services/EnrolledCourseService";
import { queryClient } from "../../main";

export function useUnEnrollStudentFromCourse() {
    const { mutate, isPending, error } = useMutation({
        mutationFn: (enrolledCourseId: number) => unenrollStudentFromCourse(enrolledCourseId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["enrolledCourses"] });
        },
        onError: (err) => {
            console.error("Error unenrolling student from course:", err instanceof Error ? err.message : err);
        },
    });

    return {
        drop: mutate,
        isDropping: isPending,
        error: error ? (error instanceof Error ? error.message : "Unknown error") : null,
    };
}
