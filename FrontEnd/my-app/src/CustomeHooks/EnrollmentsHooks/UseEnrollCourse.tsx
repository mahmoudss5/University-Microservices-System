import { useMutation } from "@tanstack/react-query";
import type { EnrolledCourseRequest } from "../../Interfaces/enrolledCourse";
import {enrollStudentInCourse} from "../../Services/EnrolledCourseService";
import { queryClient } from "../../main";
import { getUserId } from "../../Services/authService";
export const useEnrollCourse = () => {
    const userId = getUserId();
    const { mutate, isPending, error } = useMutation({
        mutationFn: async (courseId: number) => {
            const enrolledCourseRequest : EnrolledCourseRequest = {
                studentId: userId,
                courseId: courseId
            };
                
            const response = await enrollStudentInCourse(enrolledCourseRequest);
            return response.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["enrolledCourses"] });
        },
        onError: (error) => {
            console.error(error);
        },
    });
    return { mutate, isPending, error };
};