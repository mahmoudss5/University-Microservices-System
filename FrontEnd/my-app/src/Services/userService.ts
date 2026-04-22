import { decodeToken, getToken } from "./authService";
import { getTeacherDetails } from "./teacherService";
import { getStudentInfo } from "./studentService";
import type { Student } from "../Interfaces/student";
import type { Teacher } from "../Interfaces/teacher";
import type { MyTokenPayload } from "../Interfaces/Auth";
import { jwtDecode } from "jwt-decode";


export function getRole(): "teacher" | "student" {
    try {
        const { roles } = jwtDecode<MyTokenPayload>(getToken() ?? "");
        return roles.includes("Teacher") ? "teacher" : "student";
    } catch {
        return "student";
    }
}

export async function getUserDashboardData(token: string): Promise<Student | Teacher> {
    const decoded = await decodeToken(token);
    const userId = decoded.userId;
     console.log("Decoded token:", decoded);
    if (decoded.roles.includes("Teacher")) {
        const data = await getTeacherDetails(userId);
        console.log("Teacher data:", data);
        return { ...data, role: "teacher" } as Teacher;
    }

    const data = await getStudentInfo(userId);
    console.log("Student data:", data);
    return { ...data, role: "student" } as Student;
}
