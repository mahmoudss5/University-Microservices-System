import { useMutation } from "@tanstack/react-query";
import { deleteCourse } from "../../Services/CourseService";
import { getUserId } from "../../Services/authService";
import { queryClient } from "../../main";
export function useDeleteCourse() {
    const userId = getUserId();
    const { mutate, isPending, error } = useMutation({
        mutationFn: (courseId: number) => deleteCourse(courseId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["teacherCourses", userId] });
        },
    });
    if(error){
        console.error("Error deleting course:", error instanceof Error ? error.message : error);
    }
    return {
        deleteCourse: mutate,
        isPending,
        error: error ? (error instanceof Error ? error.message : "Unknown error") : null,
    };
}