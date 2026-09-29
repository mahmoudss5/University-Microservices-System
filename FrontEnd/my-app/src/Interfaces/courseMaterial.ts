export type CourseMaterialStatus = "PENDING" | "UPLOADED" | "FAILED";

export interface CourseMaterial {
    id: number;
    courseId: number;
    title: string;
    originalFilename: string;
    materialType: string;
    fileSize: number;
    status: CourseMaterialStatus;
    createdAt: string;
    uploadedAt?: string | null;
}

export interface CreateMaterialUploadRequest {
    title: string;
    originalFilename: string;
    contentType: string;
    fileSize: number;
}

export interface UploadUrlResponse {
    materialId: number;
    objectKey: string;
    uploadUrl: string;
    expiresAt: string;
}
