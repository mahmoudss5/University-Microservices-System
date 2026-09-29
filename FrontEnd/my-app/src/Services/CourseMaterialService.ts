import axios from "axios";
import { ApiUrl, getAuthHeaders } from "./config";
import type {
    CourseMaterial,
    CreateMaterialUploadRequest,
    UploadUrlResponse,
} from "../Interfaces/courseMaterial";

function getErrorMessage(error: unknown, fallback: string): Error {
    if (axios.isAxiosError(error)) {
        if (error.response) {
            return new Error(error.response.data?.message || fallback);
        }
        if (error.request) {
            return new Error("No response from server");
        }
    }

    return error instanceof Error ? error : new Error(fallback);
}

export async function getCourseMaterials(courseId: number): Promise<CourseMaterial[]> {
    try {
        const response = await axios.get<CourseMaterial[]>(
            `${ApiUrl}/api/courses/${courseId}/materials`,
            { headers: getAuthHeaders() },
        );
        return response.data;
    } catch (error) {
        throw getErrorMessage(error, "Failed to fetch course materials");
    }
}

export async function createMaterialUploadUrl(
    courseId: number,
    request: CreateMaterialUploadRequest,
): Promise<UploadUrlResponse> {
    try {
        console.log("Creating upload URL for courseId:", courseId, "with request:", request);
        const response = await axios.post<UploadUrlResponse>(
            `${ApiUrl}/api/courses/${courseId}/materials/upload-url`,
            request,
            { headers: getAuthHeaders() },
        );
        return response.data;
    } catch (error) {
        throw getErrorMessage(error, "Failed to create upload URL");
    }
}

export async function uploadToPresignedUrl(
    uploadUrl: string,
    file: File,
    contentType: string,
): Promise<void> {
    const response = await fetch(uploadUrl, {
        method: "PUT",
        headers: {
            "Content-Type": contentType,
        },
        body: file,
    });

    if (!response.ok) {
        throw new Error(`S3 upload failed with status ${response.status}`);
    }
}

export async function completeMaterialUpload(
    courseId: number,
    materialId: number,
): Promise<CourseMaterial> {
    try {
        const response = await axios.post<CourseMaterial>(
            `${ApiUrl}/api/courses/${courseId}/materials/${materialId}/complete`,
            {},
            { headers: getAuthHeaders() },
        );
        return response.data;
    } catch (error) {
        throw getErrorMessage(error, "Failed to complete material upload");
    }
}

export async function getMaterialDownloadUrl(
    courseId: number,
    materialId: number,
): Promise<string> {
    try {
        const response = await axios.get<{ downloadUrl: string }>(
            `${ApiUrl}/api/courses/${courseId}/materials/${materialId}/download-url`,
            { headers: getAuthHeaders() },
        );
        return response.data.downloadUrl;
    } catch (error) {
        throw getErrorMessage(error, "Failed to create download URL");
    }
}

export async function deleteCourseMaterial(
    courseId: number,
    materialId: number,
): Promise<void> {
    try {
        await axios.delete(
            `${ApiUrl}/api/courses/${courseId}/materials/${materialId}`,
            { headers: getAuthHeaders() },
        );
    } catch (error) {
        throw getErrorMessage(error, "Failed to delete course material");
    }
}
