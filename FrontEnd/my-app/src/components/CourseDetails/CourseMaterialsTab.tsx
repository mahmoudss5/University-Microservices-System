import { useState } from "react";
import {
    CheckCircle2,
    Download,
    File,
    FileImage,
    FileText,
    Trash2,
    Upload,
} from "lucide-react";
import { toast } from "sonner";
import LoadingSpinner from "../common/LodingSpinner";
import { useCourseMaterials } from "../../CustomeHooks/CourseDetails/UseCourseMaterials";
import type { CourseMaterial } from "../../Interfaces/courseMaterial";

interface CourseMaterialsTabProps {
    courseId: number;
    title: string;
    description: string;
    canManage: boolean;
}

const MAX_FILE_SIZE = 50 * 1024 * 1024;

export default function CourseMaterialsTab({
    courseId,
    title,
    description,
    canManage,
}: CourseMaterialsTabProps) {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [materialTitle, setMaterialTitle] = useState("");

    const {
        materials,
        isLoading,
        error,
        uploadMaterial,
        isUploading,
        deleteMaterial,
        isDeleting,
        getDownloadUrl,
        downloadingMaterialId,
        isDownloading,
    } = useCourseMaterials(courseId);

    const handleFileChange = (file: File | null) => {
        setSelectedFile(file);
        if (file && !materialTitle.trim()) {
            setMaterialTitle(file.name.replace(/\.[^/.]+$/, ""));
        }
    };

    const handleUpload = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!selectedFile) {
            toast.error("Choose a file first");
            return;
        }

        if (selectedFile.size > MAX_FILE_SIZE) {
            toast.error("The maximum file size is 50 MB");
            return;
        }

        if (!materialTitle.trim()) {
            toast.error("Enter a title for the material");
            return;
        }

        try {
            await uploadMaterial({
                title: materialTitle.trim(),
                file: selectedFile,
            });
            toast.success("Material uploaded successfully");
            setSelectedFile(null);
            setMaterialTitle("");
        } catch (uploadError) {
            toast.error(uploadError instanceof Error ? uploadError.message : "Upload failed");
        }
    };

    const handleDownload = async (material: CourseMaterial) => {
        try {
            const downloadUrl = await getDownloadUrl(material.id);
            window.open(downloadUrl, "_blank", "noopener,noreferrer");
        } catch (downloadError) {
            toast.error(
                downloadError instanceof Error
                    ? downloadError.message
                    : "Download failed",
            );
        }
    };

    const handleDelete = async (material: CourseMaterial) => {
        if (!window.confirm(`Delete ${material.title}?`)) {
            return;
        }

        try {
            await deleteMaterial(material.id);
            toast.success("Material deleted");
        } catch (deleteError) {
            toast.error(
                deleteError instanceof Error ? deleteError.message : "Delete failed",
            );
        }
    };

    return (
        <div className="space-y-6">
            <div className="bg-white rounded-2xl shadow-sm p-6">
                <div className="flex items-start justify-between gap-4 mb-6">
                    <div>
                        <h2 className="text-xl font-bold text-gray-800">{title}</h2>
                        <p className="text-sm text-gray-500 mt-1">{description}</p>
                    </div>
                    <div className="w-11 h-11 rounded-xl bg-blue-50 flex items-center justify-center">
                        <FileText className="text-blue-600" size={22} />
                    </div>
                </div>

                {canManage && (
                    <form
                        onSubmit={handleUpload}
                        className="rounded-xl border border-dashed border-blue-200 bg-blue-50/50 p-4 space-y-4"
                    >
                        <div className="flex items-center gap-2 text-sm font-semibold text-blue-800">
                            <Upload size={17} />
                            Upload course material
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-[1fr_1fr_auto] gap-3 items-end">
                            <label className="block">
                                <span className="block text-xs font-semibold text-gray-600 mb-1">
                                    Material title
                                </span>
                                <input
                                    value={materialTitle}
                                    onChange={(event) => setMaterialTitle(event.target.value)}
                                    placeholder="Week 1 lecture notes"
                                    className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                                />
                            </label>

                            <label className="block">
                                <span className="block text-xs font-semibold text-gray-600 mb-1">
                                    File
                                </span>
                                <input
                                    type="file"
                                    accept=".pdf,.png,.jpg,.jpeg,.docx"
                                    onChange={(event) => handleFileChange(event.target.files?.[0] ?? null)}
                                    className="block w-full rounded-lg border border-gray-200 bg-white px-2 py-1.5 text-sm text-gray-600 file:mr-3 file:rounded-md file:border-0 file:bg-blue-100 file:px-3 file:py-1.5 file:text-xs file:font-semibold file:text-blue-700"
                                />
                            </label>

                            <button
                                type="submit"
                                disabled={isUploading || !selectedFile}
                                className="inline-flex items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                            >
                                <Upload size={16} />
                                {isUploading ? "Uploading..." : "Upload"}
                            </button>
                        </div>

                        {selectedFile && (
                            <p className="text-xs text-gray-500">
                                Selected: {selectedFile.name} · {formatFileSize(selectedFile.size)}
                            </p>
                        )}
                    </form>
                )}
            </div>

            <div className="bg-white rounded-2xl shadow-sm p-6">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="font-bold text-gray-800">Available materials</h3>
                    <span className="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-semibold text-gray-600">
                        {materials.length} {materials.length === 1 ? "file" : "files"}
                    </span>
                </div>

                {isLoading && (
                    <div className="py-12">
                        <LoadingSpinner size="sm" text="Loading materials..." />
                    </div>
                )}

                {!isLoading && error && (
                    <div className="rounded-xl bg-red-50 p-4 text-sm text-red-700">
                        {error instanceof Error ? error.message : "Failed to load materials"}
                    </div>
                )}

                {!isLoading && !error && materials.length === 0 && (
                    <div className="rounded-xl border border-dashed border-gray-200 py-12 text-center">
                        <File className="mx-auto mb-3 text-gray-300" size={32} />
                        <p className="text-sm font-medium text-gray-500">No materials uploaded yet</p>
                        {canManage && (
                            <p className="text-xs text-gray-400 mt-1">
                                Upload the first file for this course above.
                            </p>
                        )}
                    </div>
                )}

                {!isLoading && materials.length > 0 && (
                    <div className="space-y-3">
                        {materials.map((material) => (
                            <MaterialRow
                                key={material.id}
                                material={material}
                                canManage={canManage}
                                isDownloading={isDownloading && downloadingMaterialId === material.id}
                                isDeleting={isDeleting}
                                onDownload={() => handleDownload(material)}
                                onDelete={() => handleDelete(material)}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

interface MaterialRowProps {
    material: CourseMaterial;
    canManage: boolean;
    isDownloading: boolean;
    isDeleting: boolean;
    onDownload: () => void;
    onDelete: () => void;
}

function MaterialRow({
    material,
    canManage,
    isDownloading,
    isDeleting,
    onDownload,
    onDelete,
}: MaterialRowProps) {
    const Icon = material.materialType.startsWith("image/") ? FileImage : FileText;

    return (
        <div className="flex flex-col gap-3 rounded-xl border border-gray-100 p-4 sm:flex-row sm:items-center sm:justify-between hover:border-blue-100 hover:bg-blue-50/30 transition">
            <div className="flex min-w-0 items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-gray-100">
                    <Icon size={19} className="text-gray-500" />
                </div>
                <div className="min-w-0">
                    <p className="truncate text-sm font-semibold text-gray-800">{material.title}</p>
                    <p className="truncate text-xs text-gray-500">
                        {material.originalFilename} · {formatFileSize(material.fileSize)}
                    </p>
                    <p className="mt-1 flex items-center gap-1 text-[11px] text-gray-400">
                        <CheckCircle2 size={12} className="text-green-500" />
                        Uploaded {formatDate(material.uploadedAt ?? material.createdAt)}
                    </p>
                </div>
            </div>

            <div className="flex items-center gap-2 sm:shrink-0">
                <button
                    type="button"
                    onClick={onDownload}
                    disabled={isDownloading}
                    className="inline-flex items-center gap-1.5 rounded-lg bg-blue-50 px-3 py-2 text-xs font-semibold text-blue-700 hover:bg-blue-100 disabled:cursor-wait disabled:opacity-60"
                >
                    <Download size={14} />
                    {isDownloading ? "Opening..." : "Download"}
                </button>

                {canManage && (
                    <button
                        type="button"
                        onClick={onDelete}
                        disabled={isDeleting}
                        aria-label={`Delete ${material.title}`}
                        className="inline-flex items-center justify-center rounded-lg p-2 text-red-500 hover:bg-red-50 disabled:opacity-50"
                    >
                        <Trash2 size={16} />
                    </button>
                )}
            </div>
        </div>
    );
}

function formatFileSize(bytes: number) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatDate(value: string) {
    return new Date(value).toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
    });
}
