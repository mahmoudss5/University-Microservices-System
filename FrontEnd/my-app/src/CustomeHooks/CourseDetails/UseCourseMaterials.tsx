import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    completeMaterialUpload,
    createMaterialUploadUrl,
    deleteCourseMaterial,
    getCourseMaterials,
    getMaterialDownloadUrl,
    uploadToPresignedUrl,
} from "../../Services/CourseMaterialService";
import type { CourseMaterial } from "../../Interfaces/courseMaterial";

interface UploadMaterialInput {
    title: string;
    file: File;
}

export function useCourseMaterials(courseId: number) {
    const queryClient = useQueryClient();
    const queryKey = ["courseMaterials", courseId];

    const materialsQuery = useQuery<CourseMaterial[]>({
        queryKey,
        queryFn: () => getCourseMaterials(courseId),
        enabled: courseId > 0,
    });

    const uploadMutation = useMutation({
        mutationFn: async ({ title, file }: UploadMaterialInput) => {
            const contentType = file.type;

            if (!contentType) {
                throw new Error("The selected file has no supported content type");
            }

            const upload = await createMaterialUploadUrl(courseId, {
                title,
                originalFilename: file.name,
                contentType,
                fileSize: file.size,
            });

            await uploadToPresignedUrl(upload.uploadUrl, file, contentType);

            return completeMaterialUpload(courseId, upload.materialId);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: (materialId: number) => deleteCourseMaterial(courseId, materialId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey });
        },
    });

    const downloadMutation = useMutation({
        mutationFn: (materialId: number) => getMaterialDownloadUrl(courseId, materialId),
    });

    return {
        materials: materialsQuery.data ?? [],
        isLoading: materialsQuery.isLoading,
        error: materialsQuery.error,
        uploadMaterial: uploadMutation.mutateAsync,
        isUploading: uploadMutation.isPending,
        deleteMaterial: deleteMutation.mutateAsync,
        isDeleting: deleteMutation.isPending,
        getDownloadUrl: downloadMutation.mutateAsync,
        downloadingMaterialId: downloadMutation.variables,
        isDownloading: downloadMutation.isPending,
    };
}
